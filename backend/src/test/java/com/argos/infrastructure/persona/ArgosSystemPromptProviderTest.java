package com.argos.infrastructure.persona;

import com.argos.application.gateway.JsonRpcCommandRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgosSystemPromptProviderTest {

    private final ArgosSystemPromptProvider provider = new ArgosSystemPromptProvider();

    @Test
    void getSystemPrompt_baseOnly_returnsNonEmptyStringContainingPersonaKeywords() {
        JsonRpcCommandRequest.Params params = new JsonRpcCommandRequest.Params(null, false, null, null);
        String prompt = provider.getSystemPrompt(params);

        assertThat(prompt).isNotBlank();
        assertThat(prompt).containsIgnoringCase("ARGOS");
        assertThat(prompt).containsIgnoringCase("clinical");
        assertThat(prompt).doesNotContain("Grounding");
        assertThat(prompt).doesNotContain("Tactical context");
    }

    @Test
    void getSystemPrompt_withDirectives_includesGroundingSection() {
        JsonRpcCommandRequest.Params params = new JsonRpcCommandRequest.Params("hi", true, null, "## Rule 1\nDo X.");
        String prompt = provider.getSystemPrompt(params);

        assertThat(prompt).contains("Grounding");
        assertThat(prompt).contains("local directives");
        assertThat(prompt).contains("Rule 1");
        assertThat(prompt).contains("Do X.");
    }

    @Test
    void getSystemPrompt_withInternalContext_includesTacticalSectionAndFilePaths() {
        JsonRpcCommandRequest.Params params = new JsonRpcCommandRequest.Params("review PR 1", false, "diff content", null);
        String prompt = provider.getSystemPrompt(params);

        assertThat(prompt).contains("Tactical context");
        assertThat(prompt).contains("Senior Architect");
        assertThat(prompt).contains("file path");
        assertThat(prompt).contains("line number");
        assertThat(prompt).contains("diff content");
    }
}
