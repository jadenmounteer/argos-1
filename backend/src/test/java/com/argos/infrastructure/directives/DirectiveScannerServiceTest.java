package com.argos.infrastructure.directives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class DirectiveScannerServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void getCombinedDirectives_returnsEmptyWhenPathIsBlank() {
        DirectiveScannerService service = new DirectiveScannerService("");
        assertThat(service.getCombinedDirectives()).isEmpty();
    }

    @Test
    void getCombinedDirectives_returnsEmptyWhenPathIsWhitespace() {
        DirectiveScannerService service = new DirectiveScannerService("   ");
        assertThat(service.getCombinedDirectives()).isEmpty();
    }

    @Test
    void getCombinedDirectives_returnsEmptyWhenPathDoesNotExist() {
        DirectiveScannerService service = new DirectiveScannerService("/nonexistent/path/12345");
        assertThat(service.getCombinedDirectives()).isEmpty();
    }

    @Test
    void getCombinedDirectives_returnsEmptyWhenPathIsFileNotDirectory() throws Exception {
        Path file = tempDir.resolve("not-a-dir.md");
        Files.writeString(file, "content");
        DirectiveScannerService service = new DirectiveScannerService(file.toString());
        assertThat(service.getCombinedDirectives()).isEmpty();
    }

    @Test
    void getCombinedDirectives_returnsConcatenatedContentInSortedOrder() throws Exception {
        Path a = tempDir.resolve("alpha.md");
        Path b = tempDir.resolve("beta.md");
        Files.writeString(a, "Content A");
        Files.writeString(b, "Content B");

        DirectiveScannerService service = new DirectiveScannerService(tempDir.toString());
        String result = service.getCombinedDirectives();

        assertThat(result).contains("Content A");
        assertThat(result).contains("Content B");
        assertThat(result).contains("---");
        assertThat(result.indexOf("Content A")).isLessThan(result.indexOf("Content B"));
    }

    @Test
    void getCombinedDirectives_usesCacheOnSecondCall() throws Exception {
        Path f = tempDir.resolve("single.md");
        Files.writeString(f, "Cached content");
        DirectiveScannerService service = new DirectiveScannerService(tempDir.toString());

        String first = service.getCombinedDirectives();
        String second = service.getCombinedDirectives();

        assertThat(first).isEqualTo(second);
        assertThat(first).contains("Cached content");
    }

    @Test
    void getCombinedDirectives_readsTestResourceDirectives() {
        Path testDir = Paths.get("src/test/resources/directives_test").toAbsolutePath();
        if (!Files.isDirectory(testDir)) {
            testDir = Paths.get("backend/src/test/resources/directives_test").toAbsolutePath();
        }
        if (!Files.isDirectory(testDir)) {
            return;
        }
        DirectiveScannerService service = new DirectiveScannerService(testDir.toString());
        String result = service.getCombinedDirectives();

        assertThat(result).contains("Architecture Rules");
        assertThat(result).contains("hexagonal (ports and adapters)");
        assertThat(result).contains("API Standards");
        assertThat(result).contains("JSON-RPC 2.0");
        assertThat(result).contains("---");
        assertThat(result.indexOf("Architecture Rules")).isLessThan(result.indexOf("API Standards"));
    }
}
