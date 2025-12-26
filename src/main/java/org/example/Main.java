package org.example;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Hauptklasse für die Quarkus-Anwendung
 */
@QuarkusMain
public class Main implements QuarkusApplication {

    public static void main(String... args) {
        System.out.println("Starting Calculator Application...");
        Quarkus.run(Main.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        System.out.println("Calculator Application is running!");
        System.out.println("Data collection scheduler is active and will run every 10 seconds.");
        Quarkus.waitForExit();
        return 0;
    }
}