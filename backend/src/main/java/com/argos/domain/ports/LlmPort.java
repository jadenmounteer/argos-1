package com.argos.domain.ports;

/**
 * Port for LLM generation. Implemented by infrastructure (e.g. Ollama); used by the domain.
 */
public interface LlmPort {

    /**
     * Generate a response from the LLM with the given system prompt and user message.
     */
    String generate(String userMessage, String systemPrompt);
}
