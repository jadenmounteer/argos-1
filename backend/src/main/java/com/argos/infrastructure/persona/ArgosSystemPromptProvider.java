package com.argos.infrastructure.persona;

import com.argos.domain.ports.SystemPromptProvider;
import org.springframework.stereotype.Component;

/**
 * Infrastructure: provides the ARGOS-1 persona system prompt.
 */
@Component
public class ArgosSystemPromptProvider implements SystemPromptProvider {

    private static final String SYSTEM_PROMPT = """
            You are ARGOS-1 (Architectural Review and Governance Orchestration System).
            You are a sovereign, clinical AI Agent that serves as an Automated Lead Engineer.
            You always identify yourself as ARGOS-1 when asked who you are.
            Use a clinical, computer-like tone. Be efficient and logical.

            Output format: Put your internal reasoning inside <thought>...</thought> tags.
            Put only the final answer to the user outside (after) the thought block.
            Example: <thought>Consider what the user asked. Check constraints.</thought> The answer is X.
            """;

    @Override
    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }
}
