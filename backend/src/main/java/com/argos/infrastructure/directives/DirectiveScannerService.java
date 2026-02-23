package com.argos.infrastructure.directives;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Infrastructure: reads all .md files from the configured directives path and returns
 * a single concatenated string. Caches file contents to minimize disk I/O.
 */
@Component
public class DirectiveScannerService {

    private static final Logger log = LoggerFactory.getLogger(DirectiveScannerService.class);
    private static final String SEPARATOR = "\n\n---\n\n";

    private final String directivesPath;
    private final java.util.Map<String, String> cache = new ConcurrentHashMap<>();

    public DirectiveScannerService(@Value("${argos.directives.path:}") String directivesPath) {
        this.directivesPath = directivesPath != null ? directivesPath.trim() : "";
    }

    /**
     * Reads all .md files in the configured path and returns a single concatenated string.
     * Results are cached for subsequent calls. Returns empty string if path is blank,
     * non-existent, or not a directory.
     */
    public String getCombinedDirectives() {
        if (directivesPath.isBlank()) {
            return "";
        }
        Path dir = Paths.get(directivesPath).normalize().toAbsolutePath();
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            log.warn("Directives path is not an existing directory: {}", dir);
            return "";
        }
        try (Stream<Path> stream = Files.list(dir)) {
            String combined = stream
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase().endsWith(".md"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .map(this::readOrCached)
                    .filter(s -> !s.isEmpty())
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + SEPARATOR + b);
            return combined;
        } catch (IOException e) {
            log.warn("Failed to list directives directory {}: {}", dir, e.getMessage());
            return "";
        }
    }

    private String readOrCached(Path path) {
        String key = path.toAbsolutePath().normalize().toString();
        return cache.computeIfAbsent(key, k -> {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                log.warn("Failed to read directive file {}: {}", path, e.getMessage());
                return "";
            }
        });
    }
}
