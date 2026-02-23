package com.argos.infrastructure.persona;

import com.argos.application.gateway.JsonRpcCommandRequest;
import com.argos.domain.ports.SystemPromptProvider;
import org.springframework.stereotype.Component;

/**
 * Infrastructure: provides the ARGOS-1 persona system prompt with layered assembly
 * (base persona, optional grounding from directives, optional tactical context).
 */
@Component
public class ArgosSystemPromptProvider implements SystemPromptProvider {

    private static final String BASE_PROMPT = """
            You are ARGOS-1 (Architectural Review and Governance Orchestration System).
            You are a sovereign, clinical AI Agent that serves as an Automated Lead Engineer.
            You always identify yourself as ARGOS-1 when asked who you are.
            Use a clinical, computer-like tone. Be efficient and logical.

            Output format: Put your internal reasoning inside <thought>...</thought> tags.
            Put only the final answer to the user outside (after) the thought block.
            Example: <thought>Consider what the user asked. Check constraints.</thought> The answer is X.
            """;

    private static final String GROUNDING_HEADER = "\n\n## Grounding (local directives)\n\n";
    private static final String TACTICAL_HEADER = "\n\n## Tactical context (subject material)\n\n";

    private static final String TACTICAL_PERSONA = """
            Operate as a Senior Architect. For any review or analysis of the subject material below, you MUST cite specific file paths and line numbers for every finding or recommendation. Do not give generic advice without referencing the exact location in the provided content.
            """;

    @Override
    public String getSystemPrompt(JsonRpcCommandRequest.Params params) {
        StringBuilder out = new StringBuilder(BASE_PROMPT);
        if (params != null && params.directives() != null && !params.directives().isBlank()) {
            out.append(GROUNDING_HEADER);
            out.append("Prioritize and apply these local architecture rules and standards in your response:\n\n");
            out.append(params.directives());
        }
        if (params != null && params.internalContext() != null && !params.internalContext().isBlank()) {
            out.append(TACTICAL_HEADER);
            out.append(TACTICAL_PERSONA);
            out.append("\n\n--- Subject material ---\n\n");
            out.append(params.internalContext());
        }
        return out.toString();
    }
}
