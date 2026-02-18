package com.argos.infrastructure.ollama;

import com.argos.domain.ports.LlmStreamPort;
import com.argos.domain.ports.TokenHandler;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Infrastructure adapter: implements LlmStreamPort using LangChain4j Ollama StreamingChatLanguageModel.
 */
@Component
public class OllamaStreamingAdapter implements LlmStreamPort {

    private final StreamingChatLanguageModel streamingChatModel;

    public OllamaStreamingAdapter(StreamingChatLanguageModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public void stream(String userMessage, String systemPrompt, TokenHandler handler) {
        List<dev.langchain4j.data.message.ChatMessage> messages = List.of(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userMessage)
        );
        streamingChatModel.generate(messages, new StreamingResponseHandler<>() {
            @Override
            public void onNext(String token) {
                handler.onToken(token);
            }

            @Override
            public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
                handler.onComplete();
            }

            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }
        });
    }
}
