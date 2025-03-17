import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    java
    application
    id("com.gradleup.shadow") version "9.0.0-beta10"
}

group = "fr.domotique"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val vertxVersion = "5.0.0.CR5"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "fr.domotique.MainVerticle"
val launcherClassName = "fr.domotique.Launcher"
val launcherModuleName = "fr.domotique.web"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
    mainClass.set(launcherClassName)
    mainModule.set(launcherModuleName)
}

dependencies {
    // Vert.x stuff
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-launcher-application")
    implementation("io.vertx:vertx-web")

    // Vert.x MySQL client
    implementation("io.vertx:vertx-mysql-client")
    implementation("com.ongres.scram:client:2.1")
    implementation("com.ongres.scram:scram-client:3.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.16")

    // JSON support
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    // Liquibase stuff
    implementation("org.liquibase:liquibase-core:4.27.0")
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation("com.mattbertolini:liquibase-slf4j:5.1.0")

    // Annotations
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.2")

    // Tests
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles()

    // Put all resources in the shadow jar for release.
    from("src/main/") {
        include("assets/")
        include("views/")
        into("/")
    }

    exclude("config-dev.properties")
    exclude("config-dev-local.properties")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
    }
}

// Possible thing to do for better modularity. But right now it works so who cares anyway
//tasks.named<ProcessResources>("processResources") {
//    doLast {
//        copy {
//            from(source)
//            into(project.layout.buildDirectory.dir("classes/java/main"))
//        }
//    }
//}


// Create the "updateDatabase" task to run MigrationRunner.
tasks.register<JavaExec>("updateDatabase") {
    group = "application"

    mainClass = "fr.domotique.data.MigrationRunner"

    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("update")
}

// Configure java properties (dev mode mainly) & working directory for all run tasks
tasks.withType<JavaExec> {
    jvmArgs = listOf("-Dvertxweb.environment=dev", "-Ddomotique.srcroot=" + rootDir.resolve("src/main"))
    workingDir = rootDir.resolve("src/main")
}

// This block just adds the config-dev-local.properties file if it doesn't exist
gradle.projectsEvaluated {
    val file = projectDir.resolve("src/main/resources/config-dev-local.properties")
    if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText("""
            # --- config-dev-local.properties ---
            # Contains configuration that are personal to YOU! This file won't be sent to GitHub, so you can put
            # anything personal here.
            
            # Database credentials -- fill in your username & password here!
            # Quick URL explaination: mysql://USERNAME:PASSWORD@HOST:PORT/DATABASE   [... and other random options]
            domotique.databaseUri=mysql://root:cytech1000@localhost:3306/domo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
            
            # Put a custom port (uncomment to use)
            # server.port=8080
            """.trimIndent())
        println("Created config-dev-local.properties file")
    }
}