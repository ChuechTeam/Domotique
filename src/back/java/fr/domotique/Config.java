package fr.domotique;

import io.vertx.ext.web.common.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/// The entire configuration of the Domotique server.
///
/// Contains all necessary variables to connect to services (database, mail), and some to tune various stuff.
///
/// ## Development mode
///
/// Development mode is defined using Vert.x's development mode, which is disabled by default.
/// To enable it, you should have either:
/// - `vertxweb.environment` system property set to `dev`
/// - `VERTXWEB_ENVIRONMENT` environment variable set to `dev`
///
/// ## Configuration file paths
///
/// The configuration is loaded using [#load()] through these sources:
/// - `config.properties`, `config-dev.properties` and `config-dev-local.properties`
///   in the `resources` directory (classpath)
/// - `config.properties`, `config-dev.properties` and `config-dev-local.properties`
///   in the `config` directory
/// - the file at the path given by the `domotique.configFile` system property
/// - system properties (i.e. `-Dkey=value` JVM args)
///
/// Of course, `config-dev.properties` and `config-dev-local.properties` are only loaded
/// when the server is in development mode.
///
/// @param databaseUri   `domotique.databaseUri`: the URI to the database; can't be null
/// @param port          `domotique.port`: the port to run the server on
/// @param isDevelopment `vertxweb.environment=dev` true when we're in development mode
/// @param sendGridToken `domotique.sendGridToken`: the SendGrid token to send emails; a null or blank value will default
///                                                 to the [Console email sender][fr.domotique.email.ConsoleEmail]
/// @param sendGridEmail `domotique.sendGridEmail`: the email to send emails from, must be SendGrid "Single Sender Verification"
/// @param adminCode `domotique.adminCode`: the code used to create a new admin account (default is "retraitons")
/// @author Dynamic
public record Config(
        String databaseUri,
        int port,
        boolean isDevelopment,
        @Nullable String sendGridToken,
        @Nullable String sendGridEmail,
        String adminCode
) {
    // The logger to log stuff about configuration loading.
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    // System properties
    private static final String DATABASE_URI_PROP = "domotique.databaseUri";
    private static final String PORT_PROP = "domotique.port";
    private static final String SENDGRID_TOKEN_PROP = "domotique.sendGridToken";
    private static final String SENDGRID_EMAIL_PROP = "domotique.sendGridEmail";
    private static final String ADMIN_CODE_PROP = "domotique.adminCode";

    // Constructor to check every value of the configuration.
    public Config {
        if (databaseUri == null || databaseUri.isBlank()) {
            throw new IllegalArgumentException("The Database URI (" + DATABASE_URI_PROP + ") hasn't been specified.");
        }
        if (sendGridToken != null && sendGridEmail == null) {
            throw new IllegalArgumentException("The SendGrid email (" + SENDGRID_EMAIL_PROP + ") must be specified when the token is.");
        }
    }

    /**
     * Loads the server configuration from all possible config sources. See {@link Config} for more details.
     *
     * @return the configuration
     */
    public static Config load() {
        var props = new Properties(System.getProperties());
        boolean isDevelopment = WebEnvironment.development();

        // Load the configuration from the resources
        tryLoadResource(props, "/config.properties");
        if (isDevelopment) {
            tryLoadResource(props, "/config-dev.properties");
            tryLoadResource(props, "/config-dev-local.properties");
        }

        // Load the configuration from the working directory, if these files don't exist, nothing will happen
        tryLoadFile(props, Path.of("config.properties"));
        if (isDevelopment) {
            tryLoadFile(props, Path.of("config-dev.properties"));
            tryLoadFile(props, Path.of("config-dev-local.properties"));
        }

        // Finally, load the configuration file from the domotique.configFile system property
        String configFile = System.getProperty("domotique.configFile");
        if (configFile != null) {
            try {
                props.load(Files.newInputStream(Path.of(configFile)));
                log.trace("Loaded config file from domotique.configFile '{}'", configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load required config file '" + configFile + "'", e);
            }
        }

        // Set all properties to the system properties, so we can access them from anywhere,
        // and modify non-Domotique stuff (like Vert.x!)
        System.setProperties(props);

        // Now, gather variables and go!
        String dbUri = props.getProperty(DATABASE_URI_PROP);
        String port = props.getProperty(PORT_PROP, "7777");
        int intPort;
        try {
            intPort = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The port (" + PORT_PROP + "=" + port + ") must be an integer.");
        }
        String sendGridToken = props.getProperty(SENDGRID_TOKEN_PROP);
        String sendGridEmail = props.getProperty(SENDGRID_EMAIL_PROP);
        String adminCode = props.getProperty(ADMIN_CODE_PROP, "retraitons");

        return new Config(dbUri, intPort, isDevelopment, sendGridToken, sendGridEmail, adminCode);
    }

    private static boolean tryLoadStream(Properties props, InputStream is, String fileName) {
        if (is == null) {
            return false;
        }

        try {
            props.load(is);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config file '" + fileName + "'", e);
        }
    }

    private static void tryLoadResource(Properties props, String resourceName) {
        boolean loaded = tryLoadStream(props, Config.class.getResourceAsStream(resourceName), resourceName);

        if (loaded) {
            log.trace("Loaded config file from classpath '{}'", resourceName);
        }
    }

    private static void tryLoadFile(Properties props, Path resourcePath) {
        try {
            boolean loaded = tryLoadStream(props, Files.newInputStream(resourcePath), resourcePath.toString());

            if (loaded) {
                log.trace("Loaded config file from path '{}'", resourcePath);
            }
        } catch (IOException e) {
            // If the file doesn't exist, that's expected. Else, that's just weird!
            if (Files.exists(resourcePath)) {
                throw new RuntimeException("Failed to load config file '" + resourcePath + "'", e);
            }
        }
    }
}
