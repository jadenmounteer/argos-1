package com.argos.integration;

import com.argos.adapter.rest.AgentResponse;
import com.argos.domain.model.ArgosCommand;
import com.argos.domain.model.IntelligenceService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OllamaClientIntegrationTest {

    private static final WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

    @Autowired
    private IntelligenceService intelligenceService;

    @AfterAll
    void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void registerOllamaBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("langchain4j.ollama.chat-model.base-url", () -> {
            if (!wireMockServer.isRunning()) {
                wireMockServer.start();
            }
            return wireMockServer.baseUrl();
        });
    }

    @Test
    void process_delegatesToOllamaViaLangChain4j() {
        // Stub Ollama /api/chat endpoint with a minimal valid response body.
        String responseBody = """
            {
              "model": "deepseek-r1:8b",
              "created_at": "2026-01-01T00:00:00Z",
              "message": {
                "role": "assistant",
                "content": "Stubbed Ollama response"
              },
              "done": true
            }
            """;

        wireMockServer.stubFor(post(urlPathEqualTo("/api/chat"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        AgentResponse response = intelligenceService.process(new ArgosCommand("Hello from integration test"));

        assertThat(response.response()).contains("Stubbed Ollama response");
    }
}

