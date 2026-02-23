package com.argos.domain.ports;

/**
 * Port for combined directive content (e.g. from local Markdown). Implemented by infrastructure; used by the application layer.
 */
public interface DirectivePort {

    /**
     * Returns all combined directives as a single string (e.g. concatenated .md files).
     */
    String getCombinedDirectives();
}
