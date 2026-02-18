package com.argos.infrastructure.ollama;

import com.argos.domain.ports.LlmPort;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Infrastructure adapter: implements LlmPort using LangChain4j Ollama ChatLanguageModel.
 */
@Component
public class OllamaChatAdapter implements LlmPort {

    private final ChatLanguageModel chatModel;

    public OllamaChatAdapter(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generate(String userMessage, String systemPrompt) {
        List<dev.langchain4j.data.message.ChatMessage> messages = List.of(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userMessage)
        );
        Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages);
        return response.content().text();
    }
}
