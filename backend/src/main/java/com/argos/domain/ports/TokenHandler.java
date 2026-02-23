package com.argos.domain.ports;

/**
 * Callback for consuming a stream of LLM tokens. Used by LlmStreamPort.
 * handleThought allows the gateway to push synthetic status messages (e.g. "Fetching PR Data...") before the LLM runs.
 */
public interface TokenHandler {

    void onToken(String token);

    void onComplete();

    void onError(Throwable error);

    /**
     * Push a status thought to the client. Called by the application layer before or during orchestration.
     */
    void handleThought(String text);
}
