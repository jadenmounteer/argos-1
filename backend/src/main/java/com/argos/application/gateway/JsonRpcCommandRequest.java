package com.argos.application.gateway;

/**
 * JSON-RPC 2.0-aligned request body for POST /api/v1/command.
 */
public record JsonRpcCommandRequest(
        String jsonrpc,
        Object id,
        String method,
        Params params
) {
    public record Params(String input) {
    }

    public String getInput() {
        return params != null ? params.input() : null;
    }
}
