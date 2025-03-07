package fr.domotique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entire web app. Magically sets up the services and controllers, and spins up a server.
 *
 * @author Dynamic
 */
@SpringBootApplication
public class DomotiqueApplication {
    /**
     * You know. The main function. It runs the web server...
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {
        SpringApplication.run(DomotiqueApplication.class, args);
    }
}
