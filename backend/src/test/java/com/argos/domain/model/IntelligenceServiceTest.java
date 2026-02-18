package com.argos.domain.model;

import com.argos.adapter.rest.AgentResponse;
import com.argos.domain.ports.LlmPort;
import com.argos.domain.ports.SystemPromptProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligenceServiceTest {

    private static final String SYSTEM_PROMPT = "You are ARGOS-1.";

    @Mock
    private LlmPort llmPort;

    @Mock
    private SystemPromptProvider systemPromptProvider;

    @Test
    void process_callsProviderAndPort_returnsAgentResponseWithLlmOutput() {
        when(systemPromptProvider.getSystemPrompt()).thenReturn(SYSTEM_PROMPT);
        when(llmPort.generate(eq("test input"), eq(SYSTEM_PROMPT))).thenReturn("LLM output");

        IntelligenceService service = new IntelligenceService(llmPort, systemPromptProvider);
        AgentResponse response = service.process(new ArgosCommand("test input"));

        assertThat(response.thought()).isEmpty();
        assertThat(response.action()).isEmpty();
        assertThat(response.response()).isEqualTo("LLM output");
    }

    @Test
    void verifyIdentity_whenLlmResponseContainsArgos1_returnsTrue() {
        when(systemPromptProvider.getSystemPrompt()).thenReturn(SYSTEM_PROMPT);
        when(llmPort.generate(anyString(), eq(SYSTEM_PROMPT))).thenReturn("I am ARGOS-1, the Automated Lead Engineer.");

        IntelligenceService service = new IntelligenceService(llmPort, systemPromptProvider);
        assertThat(service.verifyIdentity()).isTrue();
    }

    @Test
    void verifyIdentity_whenLlmResponseDoesNotContainArgos1_returnsFalse() {
        when(systemPromptProvider.getSystemPrompt()).thenReturn(SYSTEM_PROMPT);
        when(llmPort.generate(anyString(), eq(SYSTEM_PROMPT))).thenReturn("I am a generic assistant.");

        IntelligenceService service = new IntelligenceService(llmPort, systemPromptProvider);
        assertThat(service.verifyIdentity()).isFalse();
    }
}
