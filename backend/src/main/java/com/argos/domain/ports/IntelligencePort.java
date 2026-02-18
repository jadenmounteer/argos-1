package com.argos.domain.ports;

import com.argos.domain.model.ArgosCommand;
import com.argos.adapter.rest.AgentResponse;

/**
 * Port for processing user commands and identity verification. Implemented by the domain; used by the adapter.
 */
public interface IntelligencePort {

    AgentResponse process(ArgosCommand command);

    /**
     * Verify that the LLM correctly identifies itself as ARGOS-1.
     */
    boolean verifyIdentity();
}
