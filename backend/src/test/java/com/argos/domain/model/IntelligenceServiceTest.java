package com.argos.domain.model;

import com.argos.adapter.rest.AgentResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntelligenceServiceTest {

    private final IntelligenceService service = new IntelligenceService();

    @Test
    void process_returnsStubAgentResponse() {
        ArgosCommand command = new ArgosCommand("test input");

        AgentResponse response = service.process(command);

        assertThat(response.thought()).isEmpty();
        assertThat(response.action()).isEmpty();
        assertThat(response.response())
                .contains("Acknowledged")
                .contains("test input")
                .contains("LLM integration pending");
    }
}
