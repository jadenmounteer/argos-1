package com.argos.domain.model;

import com.argos.domain.ports.IntelligencePort;
import com.argos.adapter.rest.AgentResponse;
import org.springframework.stereotype.Service;

/**
 * Core intelligence: processes commands. Implements IntelligencePort.
 * Task 3: stub response; Task 4 will add LLM integration.
 */
@Service
public class IntelligenceService implements IntelligencePort {

    @Override
    public AgentResponse process(ArgosCommand command) {
        return new AgentResponse(
                "",
                "",
                "Acknowledged. Command received: \"" + command.input() + "\". LLM integration pending."
        );
    }
}
