package com.argos.infrastructure.ollama;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaChatAdapterTest {

    @Mock
    private ChatLanguageModel chatModel;

    @Test
    void generate_buildsMessagesAndReturnsModelResponse() {
        when(chatModel.generate(anyList())).thenReturn(Response.from(AiMessage.from("Model response")));

        OllamaChatAdapter adapter = new OllamaChatAdapter(chatModel);
        String result = adapter.generate("user message", "system prompt");

        assertThat(result).isEqualTo("Model response");
    }
}
