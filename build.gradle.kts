import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("gg.jte.gradle") version "3.1.16"
}

group = "fr.domotique"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("gg.jte:jte:3.1.16")
    implementation("gg.jte:jte-spring-boot-starter-3:3.1.16")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

jte {
    generate()
    binaryStaticContent = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootRun> {
    // Make sure we use the development profile!
    jvmArgs = listOf("-Dspring.profiles.active=dev")
}

// This block just adds the application-dev-local.properties file if it doesn't exist
gradle.projectsEvaluated {
    val file = projectDir.resolve("src/main/resources/application-dev-local.properties")
    if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText("""
            # ----- Database credentials -----
            
            # Name and address of the database (default name is domo; automatically created!)
            # 3306 is the default port for MySQL, change it if you... changed it?
            spring.datasource.url=jdbc:mysql://localhost:3306/domo?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
            
            # Username and password to use -- Replace this with the right password/username!
            spring.datasource.username=root
            spring.datasource.password=cytech1000
            
            # Put a custom port (uncomment to use)
            # server.port=8080
            """.trimIndent())
        println("Created application-dev-local.properties file")
    }
}