package com.argos.domain.ports;

import com.argos.application.gateway.JsonRpcCommandRequest;

/**
 * Port for the ARGOS-1 system prompt (persona). Implemented by infrastructure; used by the application layer.
 * Params may carry server-hydrated directives and internalContext for layered assembly.
 */
public interface SystemPromptProvider {

    String getSystemPrompt(JsonRpcCommandRequest.Params params);
}
