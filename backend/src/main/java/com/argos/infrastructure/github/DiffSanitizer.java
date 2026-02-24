package com.argos.infrastructure.github;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Sanitizes raw PR diff text for LLM consumption: removes noisy sections (lockfiles,
 * .gitignore, binary), optionally strips hunk markers on large diffs, and enforces a max length.
 */
@Component
public class DiffSanitizer {

    private static final int MAX_DIFF_LENGTH = 20_000;
    private static final int HUNK_STRIP_THRESHOLD = 15_000;
    private static final String TRUNCATED_MARKER = "\n[TRUNCATED]";

    private static final Pattern DIFF_GIT_HEADER = Pattern.compile("(?m)^diff --git ");
    /** Path in "diff --git a/PATH b/PATH" or "a/PATH" from first line of section. */
    private static final Pattern PATH_IN_GIT_HEADER = Pattern.compile("^a/(.+?) b/");
    private static final Pattern HUNK_HEADER = Pattern.compile("(?m)^@@ -[\\d,]+ \\+[\\d,]+ @@.*$");
    private static final Pattern BINARY_LINE = Pattern.compile("(?m)^Binary files .+ differ\\r?\\n?");

    /**
     * Cleans raw diff: drops unwanted file sections, optionally strips @@ hunks if large, then truncates to max length.
     *
     * @param rawDiff raw PR diff (e.g. from GitHub); may be null or empty
     * @return cleaned diff, or empty string if null/empty input
     */
    public String sanitize(String rawDiff) {
        if (rawDiff == null || rawDiff.isEmpty()) {
            return "";
        }

        String afterSkip = applySkipLogic(rawDiff);
        String afterHunk = applyHunkManagement(afterSkip);
        return applySizeGuard(afterHunk);
    }

    private String applySkipLogic(String rawDiff) {
        String[] sections = DIFF_GIT_HEADER.split(rawDiff, -1);
        if (sections.length == 0) {
            return rawDiff;
        }
        StringBuilder out = new StringBuilder();
        if (!sections[0].isEmpty()) {
            out.append(removeBinaryLines(sections[0]));
        }
        for (int i = 1; i < sections.length; i++) {
            String section = sections[i];
            if (section.isEmpty()) {
                continue;
            }
            if (shouldSkipSection(section)) {
                continue;
            }
            if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
                out.append('\n');
            }
            out.append("diff --git ").append(section);
            if (!section.endsWith("\n")) {
                out.append('\n');
            }
        }
        return out.toString();
    }

    private boolean shouldSkipSection(String section) {
        int firstNewline = section.indexOf('\n');
        String firstLine = firstNewline >= 0 ? section.substring(0, firstNewline) : section;
        String path = extractPathFromHeader(firstLine);
        if (path != null) {
            if (path.endsWith("package-lock.json") || path.equals(".gitignore") || path.endsWith("/.gitignore")) {
                return true;
            }
            if (section.contains("Binary files ") && section.contains(" differ")) {
                return true;
            }
        }
        return false;
    }

    /** Remove lines that are "Binary files ... differ" (for preamble / no diff --git case). */
    private String removeBinaryLines(String text) {
        return BINARY_LINE.matcher(text).replaceAll("");
    }

    private String extractPathFromHeader(String firstLine) {
        java.util.regex.Matcher m = PATH_IN_GIT_HEADER.matcher(firstLine);
        return m.find() ? m.group(1) : null;
    }

    private String applyHunkManagement(String diff) {
        if (diff.length() <= HUNK_STRIP_THRESHOLD) {
            return diff;
        }
        String withoutHunks = HUNK_HEADER.matcher(diff).replaceAll("");
        return withoutHunks.replaceAll("\n{3,}", "\n\n");
    }

    private String applySizeGuard(String diff) {
        if (diff.length() <= MAX_DIFF_LENGTH) {
            return diff;
        }
        return diff.substring(0, MAX_DIFF_LENGTH) + TRUNCATED_MARKER;
    }
}
