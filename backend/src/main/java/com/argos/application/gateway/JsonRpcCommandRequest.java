package com.argos.application.gateway;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * JSON-RPC 2.0-aligned request body for POST /api/v1/command.
 * internalContext and directives are server-side only; never sent or accepted from the client.
 */
public record JsonRpcCommandRequest(
        String jsonrpc,
        Object id,
        String method,
        Params params
) {
    public record Params(
            String input,
            Boolean isGrounded,
            @JsonIgnore String internalContext,
            @JsonIgnore String directives
    ) {
    }

    public String getInput() {
        return params != null ? params.input() : null;
    }

    public Boolean getIsGrounded() {
        return params != null ? params.isGrounded() : null;
    }
}
