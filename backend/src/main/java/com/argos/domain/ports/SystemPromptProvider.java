package com.argos.domain.ports;

/**
 * Port for the ARGOS-1 system prompt (persona). Implemented by infrastructure; used by the domain.
 */
public interface SystemPromptProvider {

    String getSystemPrompt();
}
