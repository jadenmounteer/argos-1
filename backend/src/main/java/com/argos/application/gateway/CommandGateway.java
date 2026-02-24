package com.argos.application.gateway;

import com.argos.domain.ports.DirectivePort;
import com.argos.domain.ports.GitRepoPort;
import com.argos.domain.ports.LlmStreamPort;
import com.argos.domain.ports.SystemPromptProvider;
import com.argos.domain.ports.TokenHandler;
import com.argos.infrastructure.github.DiffSanitizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Application layer: JSON-RPC bridge and SSE stream. Orchestrates LlmStreamPort, SystemPromptProvider,
 * DirectivePort, and GitRepoPort. Hydrates params (PR diff, directives) and assembles system prompt.
 */
@Service
public class CommandGateway {

    private static final String THOUGHT_OPEN = "<thought>";
    private static final String THOUGHT_CLOSE = "</thought>";
    private static final Pattern PR_INTENT = Pattern.compile(
            "(?i)(?:review|analyze|check|scan)\\s+(?:pull\\s*request|pr)\\s*(?:number\\s+)?#?((?:\\d+|\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|ten)\\b)(?:[\\s-]+(?:\\d+|\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|ten)\\b))*)");

    private final LlmStreamPort llmStreamPort;
    private final SystemPromptProvider systemPromptProvider;
    private final DirectivePort directivePort;
    private final GitRepoPort gitRepoPort;
    private final DiffSanitizer diffSanitizer;
    private final long sseTimeoutMs;

    public CommandGateway(LlmStreamPort llmStreamPort,
                          SystemPromptProvider systemPromptProvider,
                          DirectivePort directivePort,
                          GitRepoPort gitRepoPort,
                          DiffSanitizer diffSanitizer,
                          @Value("${argos.sse.timeout-ms:60000}") long sseTimeoutMs) {
        this.llmStreamPort = llmStreamPort;
        this.systemPromptProvider = systemPromptProvider;
        this.directivePort = directivePort;
        this.gitRepoPort = gitRepoPort;
        this.diffSanitizer = diffSanitizer;
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
        Object requestId = request.id();

        TokenHandler handler = new TokenHandler() {
            final StringBuilder buffer = new StringBuilder();
            boolean inThought = false;
            final StringBuilder thoughtAccumulator = new StringBuilder();
            final StringBuilder responseAccumulator = new StringBuilder();

            @Override
            public void handleThought(String text) {
                if (text != null && !text.isEmpty()) {
                    thoughtAccumulator.append(text);
                    emitNotification(emitter, "thought", Map.of("text", text));
                }
            }

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

        Integer prId = null;
        Matcher prMatcher = PR_INTENT.matcher(input);
        if (prMatcher.find()) {
            try {
                prId = resolvePrId(prMatcher.group(1).trim());
            } catch (Exception ignored) {
            }
        }

        String internalContext = "";
        if (prId != null) {
            handler.handleThought("COMMAND DETECTED: Fetching PR Data...");
            try {
                internalContext = gitRepoPort.getRawDiff(prId.intValue());
                if (internalContext == null) {
                    internalContext = "";
                }
                internalContext = diffSanitizer.sanitize(internalContext);
            } catch (Exception ignored) {
                internalContext = "";
            }
        }

        boolean isGrounded = Boolean.TRUE.equals(request.getIsGrounded());
        boolean needDirectives = isGrounded || prId != null;
        String directives = "";
        if (needDirectives) {
            handler.handleThought("Loading directives...");
            directives = directivePort.getCombinedDirectives();
            if (directives == null) {
                directives = "";
            }
        }

        JsonRpcCommandRequest.Params enrichedParams = new JsonRpcCommandRequest.Params(
                input,
                isGrounded,
                internalContext,
                directives);
        String systemPrompt = systemPromptProvider.getSystemPrompt(enrichedParams);

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

    /**
     * Resolves captured PR id string (digits and/or number-words) to an int.
     * E.g. "one one five ten" -> 11510, "1 1 5 10" -> 11510.
     * @throws NumberFormatException if no valid parts or parse fails
     */
    private static int resolvePrId(String capturedValue) {
        Map<String, String> wordToDigit = Map.ofEntries(
                entry("zero", "0"), entry("one", "1"), entry("two", "2"), entry("three", "3"),
                entry("four", "4"), entry("five", "5"), entry("six", "6"), entry("seven", "7"),
                entry("eight", "8"), entry("nine", "9"), entry("ten", "10"));
        String[] parts = capturedValue.toLowerCase().trim().split("[\\s-]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (wordToDigit.containsKey(part)) {
                sb.append(wordToDigit.get(part));
            } else if (part.matches("\\d+")) {
                sb.append(part);
            }
        }
        if (sb.length() == 0) {
            throw new NumberFormatException("No valid PR id parts: " + capturedValue);
        }
        return Integer.parseInt(sb.toString());
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
