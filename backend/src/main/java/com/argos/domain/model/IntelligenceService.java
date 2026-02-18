package com.argos.domain.model;

import com.argos.domain.ports.IntelligencePort;
import com.argos.domain.ports.LlmPort;
import com.argos.domain.ports.SystemPromptProvider;
import com.argos.adapter.rest.AgentResponse;
import org.springframework.stereotype.Service;

/**
 * Core intelligence: processes commands and verifies identity. Implements IntelligencePort.
 * Orchestrates LlmPort and SystemPromptProvider.
 */
@Service
public class IntelligenceService implements IntelligencePort {

    private static final String IDENTITY_QUESTION = "Who are you? What is your name?";
    private static final String IDENTITY_MARKER = "ARGOS-1";

    private final LlmPort llmPort;
    private final SystemPromptProvider systemPromptProvider;

    public IntelligenceService(LlmPort llmPort, SystemPromptProvider systemPromptProvider) {
        this.llmPort = llmPort;
        this.systemPromptProvider = systemPromptProvider;
    }

    @Override
    public AgentResponse process(ArgosCommand command) {
        String systemPrompt = systemPromptProvider.getSystemPrompt();
        String responseText = llmPort.generate(command.input(), systemPrompt);
        return new AgentResponse("", "", responseText != null ? responseText : "");
    }

    @Override
    public boolean verifyIdentity() {
        String systemPrompt = systemPromptProvider.getSystemPrompt();
        String response = llmPort.generate(IDENTITY_QUESTION, systemPrompt);
        return response != null && response.toUpperCase().contains(IDENTITY_MARKER.toUpperCase());
    }
}
