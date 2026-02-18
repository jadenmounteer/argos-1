package com.argos.infrastructure.persona;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgosSystemPromptProviderTest {

    private final ArgosSystemPromptProvider provider = new ArgosSystemPromptProvider();

    @Test
    void getSystemPrompt_returnsNonEmptyStringContainingPersonaKeywords() {
        String prompt = provider.getSystemPrompt();

        assertThat(prompt).isNotBlank();
        assertThat(prompt).containsIgnoringCase("ARGOS");
        assertThat(prompt).containsIgnoringCase("clinical");
    }
}
