package com.argos.domain.ports;

/**
 * Callback for consuming a stream of LLM tokens. Used by LlmStreamPort.
 */
public interface TokenHandler {

    void onToken(String token);

    void onComplete();

    void onError(Throwable error);
}
