package com.argos.infrastructure.github;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiffSanitizerTest {

    private final DiffSanitizer sanitizer = new DiffSanitizer();

    @Test
    void sanitize_returnsEmptyForNull() {
        assertThat(sanitizer.sanitize(null)).isEmpty();
    }

    @Test
    void sanitize_returnsEmptyForEmptyString() {
        assertThat(sanitizer.sanitize("")).isEmpty();
    }

    @Test
    void sanitize_removesPackageLockSection() {
        String diff = "diff --git a/package-lock.json b/package-lock.json\n"
                + "index 111..222 100644\n"
                + "--- a/package-lock.json\n"
                + "+++ b/package-lock.json\n"
                + "@@ -1,2 +1,2 @@\n"
                + "- \"old\"\n"
                + "+ \"new\"\n"
                + "diff --git a/src/Foo.java b/src/Foo.java\n"
                + "index aaa..bbb 100644\n"
                + "--- a/src/Foo.java\n"
                + "+++ b/src/Foo.java\n"
                + "@@ -1,1 +1,1 @@\n"
                + "+ public class Foo {}\n";
        String result = sanitizer.sanitize(diff);
        assertThat(result).doesNotContain("package-lock.json");
        assertThat(result).contains("src/Foo.java");
        assertThat(result).contains("public class Foo");
    }

    @Test
    void sanitize_removesGitignoreSection() {
        String diff = "diff --git a/.gitignore b/.gitignore\n"
                + "--- a/.gitignore\n"
                + "+++ b/.gitignore\n"
                + "@@ -1,1 +1,2 @@\n"
                + "+ .env\n";
        String result = sanitizer.sanitize(diff);
        assertThat(result).isEmpty();
    }

    @Test
    void sanitize_removesBinaryFileSection() {
        String diff = "diff --git a/image.png b/image.png\n"
                + "index 111..222 100644\n"
                + "Binary files a/image.png and b/image.png differ\n";
        String result = sanitizer.sanitize(diff);
        assertThat(result).doesNotContain("Binary files");
        assertThat(result).doesNotContain("image.png");
    }

    @Test
    void sanitize_keepsHunkMarkersWhenSmall() {
        String diff = "diff --git a/Foo.java b/Foo.java\n"
                + "--- a/Foo.java\n"
                + "+++ b/Foo.java\n"
                + "@@ -1,3 +1,4 @@\n"
                + " public class Foo {\n"
                + "+  void bar() {}\n"
                + " }\n";
        String result = sanitizer.sanitize(diff);
        assertThat(result).contains("@@");
        assertThat(result).contains("public class Foo");
    }

    @Test
    void sanitize_stripsHunkMarkersWhenLarge() {
        StringBuilder big = new StringBuilder();
        big.append("diff --git a/Big.java b/Big.java\n");
        big.append("--- a/Big.java\n+++ b/Big.java\n");
        for (int i = 0; i < 800; i++) {
            big.append("@@ -1,1 +1,1 @@\n+line ").append(i).append("\n");
        }
        String diff = big.toString();
        assertThat(diff.length()).isGreaterThan(15_000);
        String result = sanitizer.sanitize(diff);
        assertThat(result).doesNotContain("@@");
        assertThat(result).contains("line");
    }

    @Test
    void sanitize_truncatesAndAddsMarkerWhenOverMaxLength() {
        String longContent = "x".repeat(25_000);
        String result = sanitizer.sanitize(longContent);
        assertThat(result).hasSize(20_000 + "\n[TRUNCATED]".length());
        assertThat(result).endsWith("\n[TRUNCATED]");
        assertThat(result).startsWith("xxx");
    }

    @Test
    void sanitize_noDiffGitButBinaryLine_removesBinaryPreservesRest() {
        String diff = "some preamble\nBinary files a/x b/x differ\nmore text";
        String result = sanitizer.sanitize(diff);
        assertThat(result).doesNotContain("Binary files");
        assertThat(result).contains("some preamble");
        assertThat(result).contains("more text");
    }

    @Test
    void sanitize_multipleSectionsKeepsNonNoise() {
        String diff = "diff --git a/.gitignore b/.gitignore\n"
                + "--- a/.gitignore\n+++ b/.gitignore\n@@ -1,0 +1,1 @@\n+*.log\n"
                + "diff --git a/README.md b/README.md\n"
                + "--- a/README.md\n+++ b/README.md\n@@ -1,0 +1,1 @@\n+# Hello\n";
        String result = sanitizer.sanitize(diff);
        assertThat(result).doesNotContain(".gitignore");
        assertThat(result).contains("README.md");
        assertThat(result).contains("# Hello");
    }
}
