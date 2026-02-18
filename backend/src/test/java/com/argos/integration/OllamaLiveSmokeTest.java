package com.argos.integration;

import com.argos.adapter.rest.AgentResponse;
import com.argos.domain.model.ArgosCommand;
import com.argos.domain.model.IntelligenceService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Optional live smoke test that calls a real local Ollama instance.
 * Enable explicitly when Ollama is running at the configured base URL.
 */
@SpringBootTest
@ActiveProfiles("ollama-live")
@Disabled("Enable when Ollama is running locally")
class OllamaLiveSmokeTest {

    @Autowired
    private IntelligenceService intelligenceService;

    @Test
    void liveCall_returnsNonEmptyResponse() {
        AgentResponse response = intelligenceService.process(new ArgosCommand("Identify yourself as ARGOS-1."));
        assertThat(response.response()).isNotBlank();
    }
}

