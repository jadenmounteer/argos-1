package com.argos.domain.ports;

import com.argos.domain.model.ArgosCommand;
import com.argos.adapter.rest.AgentResponse;

/**
 * Port for processing user commands. Implemented by the domain; used by the adapter.
 */
public interface IntelligencePort {

    AgentResponse process(ArgosCommand command);
}
