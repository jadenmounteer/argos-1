package com.argos.application.gateway;

import com.argos.domain.ports.LlmStreamPort;
import com.argos.domain.ports.SystemPromptProvider;
import com.argos.domain.ports.TokenHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * Application layer: JSON-RPC bridge and SSE stream. Orchestrates LlmStreamPort and SystemPromptProvider.
 */
@Service
public class CommandGateway {

    private static final String THOUGHT_OPEN = "<thought>";
    private static final String THOUGHT_CLOSE = "</thought>";

    private final LlmStreamPort llmStreamPort;
    private final SystemPromptProvider systemPromptProvider;
    private final long sseTimeoutMs;

    public CommandGateway(LlmStreamPort llmStreamPort,
                          SystemPromptProvider systemPromptProvider,
                          @Value("${argos.sse.timeout-ms:60000}") long sseTimeoutMs) {
        this.llmStreamPort = llmStreamPort;
        this.systemPromptProvider = systemPromptProvider;
        this.sseTimeoutMs = sseTimeoutMs;
    }

    public SseEmitter runStream(JsonRpcCommandRequest request) {
        SseEmitter emitter = new SseEmitter(sseTimeoutMs);

        emitter.onTimeout(() -> {
            emitter.complete();
        });
        emitter.onCompletion(() -> {
            // no-op; emitter is not retained
        });
        emitter.onError((ex) -> {
            try {
                emitter.completeWithError(ex);
            } catch (Exception ignored) {
            }
        });

        String input = request.getInput() != null ? request.getInput() : "";
        String systemPrompt = systemPromptProvider.getSystemPrompt();
        Object requestId = request.id();

        TokenHandler handler = new TokenHandler() {
            final StringBuilder buffer = new StringBuilder();
            boolean inThought = false;
            final StringBuilder thoughtAccumulator = new StringBuilder();
            final StringBuilder responseAccumulator = new StringBuilder();

            @Override
            public void onToken(String token) {
                buffer.append(token);
                drainBuffer(emitter);
            }

            private void drainBuffer(SseEmitter emitter) {
                while (true) {
                    if (inThought) {
                        int closeIdx = buffer.indexOf(THOUGHT_CLOSE);
                        if (closeIdx >= 0) {
                            String thoughtChunk = buffer.substring(0, closeIdx);
                            buffer.delete(0, closeIdx + THOUGHT_CLOSE.length());
                            thoughtAccumulator.append(thoughtChunk);
                            if (!thoughtChunk.isEmpty()) {
                                emitNotification(emitter, "thought", Map.of("text", thoughtChunk));
                            }
                            inThought = false;
                            continue;
                        }
                        String chunk = buffer.toString();
                        if (chunk.contains("<") || chunk.contains(">")) {
                            int safeEnd = Math.min(
                                    chunk.indexOf('<') >= 0 ? chunk.indexOf('<') : chunk.length(),
                                    chunk.indexOf('>') >= 0 ? chunk.indexOf('>') + 1 : chunk.length()
                            );
                            if (safeEnd > 0) {
                                String thoughtChunk = chunk.substring(0, safeEnd);
                                buffer.delete(0, safeEnd);
                                thoughtAccumulator.append(thoughtChunk);
                                emitNotification(emitter, "thought", Map.of("text", thoughtChunk));
                            }
                            return;
                        }
                        if (!chunk.isEmpty()) {
                            thoughtAccumulator.append(chunk);
                            buffer.setLength(0);
                            emitNotification(emitter, "thought", Map.of("text", chunk));
                        }
                        return;
                    } else {
                        int openIdx = buffer.indexOf(THOUGHT_OPEN);
                        if (openIdx >= 0) {
                            String responseChunk = buffer.substring(0, openIdx);
                            buffer.delete(0, openIdx + THOUGHT_OPEN.length());
                            responseAccumulator.append(responseChunk);
                            if (!responseChunk.isEmpty()) {
                                emitNotification(emitter, "response", Map.of("text", responseChunk));
                            }
                            inThought = true;
                            continue;
                        }
                        String chunk = buffer.toString();
                        if (chunk.contains("<")) {
                            int safeEnd = chunk.indexOf('<');
                            if (safeEnd > 0) {
                                String responseChunk = chunk.substring(0, safeEnd);
                                buffer.delete(0, safeEnd);
                                responseAccumulator.append(responseChunk);
                                emitNotification(emitter, "response", Map.of("text", responseChunk));
                            }
                            return;
                        }
                        if (!chunk.isEmpty()) {
                            responseAccumulator.append(chunk);
                            buffer.setLength(0);
                            emitNotification(emitter, "response", Map.of("text", chunk));
                        }
                        return;
                    }
                }
            }

            @Override
            public void onComplete() {
                if (buffer.length() > 0) {
                    String remainder = buffer.toString();
                    if (inThought) {
                        emitNotification(emitter, "thought", Map.of("text", remainder));
                        thoughtAccumulator.append(remainder);
                    } else {
                        emitNotification(emitter, "response", Map.of("text", remainder));
                        responseAccumulator.append(remainder);
                    }
                }
                emitActionPlaceholder(emitter);
                emitResult(emitter, requestId, thoughtAccumulator.toString(), responseAccumulator.toString());
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of(
                            "jsonrpc", "2.0",
                            "error", Map.of("code", -32603, "message", error.getMessage())
                    )));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(error);
            }
        };

        new Thread(() -> {
            try {
                llmStreamPort.stream(input, systemPrompt, handler);
            } catch (Exception e) {
                handler.onError(e);
            }
        }, "argos-stream").start();

        return emitter;
    }

    private void emitNotification(SseEmitter emitter, String method, Map<String, ?> params) {
        try {
            Map<String, Object> payload = Map.of(
                    "jsonrpc", "2.0",
                    "method", method,
                    "params", params
            );
            emitter.send(SseEmitter.event().name(method).data(payload));
        } catch (IOException e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
        }
    }

    private void emitActionPlaceholder(SseEmitter emitter) {
        emitNotification(emitter, "action", Map.of("type", "none"));
    }

    private void emitResult(SseEmitter emitter, Object requestId, String thought, String response) {
        try {
            Map<String, Object> result = Map.of(
                    "thought", thought != null ? thought : "",
                    "action", "",
                    "response", response != null ? response : ""
            );
            Map<String, Object> payload = Map.of(
                    "jsonrpc", "2.0",
                    "id", requestId != null ? requestId : Integer.valueOf(0),
                    "result", result
            );
            emitter.send(SseEmitter.event().name("result").data(payload));
        } catch (IOException e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
        }
    }
}
