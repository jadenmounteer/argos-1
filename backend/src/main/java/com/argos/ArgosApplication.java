package com.argos;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ArgosApplication {

    public static void main(String[] args) {
        loadDotenvFromMonorepoRoot();
        SpringApplication.run(ArgosApplication.class, args);
    }

    /**
     * Load .env from the monorepo root into system properties so application.properties
     * placeholders (e.g. ${ARGOS_DIRECTIVES_PATH}) resolve. Tries ../.env when run from
     * backend/ and .env when run from repo root. Does not overwrite existing env vars.
     */
    private static void loadDotenvFromMonorepoRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path repoRoot = cwd.getParent();

        Dotenv dotenv = null;
        if (repoRoot != null && repoRoot.resolve(".env").toFile().exists()) {
            dotenv = Dotenv.configure().directory(repoRoot.toString()).load();
        }
        if (dotenv == null && cwd.resolve(".env").toFile().exists()) {
            dotenv = Dotenv.configure().directory(cwd.toString()).load();
        }
        if (dotenv == null) {
            return;
        }
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
