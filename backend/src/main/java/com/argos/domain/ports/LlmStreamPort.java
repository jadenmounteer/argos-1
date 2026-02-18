package com.argos.domain.ports;

/**
 * Port for streaming LLM output. Implemented by infrastructure (e.g. Ollama); used by the application layer.
 */
public interface LlmStreamPort {

    /**
     * Stream a response from the LLM; invokes the handler for each token and on completion/error.
     */
    void stream(String userMessage, String systemPrompt, TokenHandler handler);
}
