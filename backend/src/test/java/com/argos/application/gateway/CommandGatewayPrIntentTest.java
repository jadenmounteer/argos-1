package com.argos.application.gateway;

import com.argos.domain.ports.DirectivePort;
import com.argos.domain.ports.GitRepoPort;
import com.argos.domain.ports.LlmStreamPort;
import com.argos.domain.ports.SystemPromptProvider;
import com.argos.infrastructure.github.DiffSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandGatewayPrIntentTest {

    @Mock
    private LlmStreamPort llmStreamPort;
    @Mock
    private SystemPromptProvider systemPromptProvider;
    @Mock
    private DirectivePort directivePort;
    @Mock
    private GitRepoPort gitRepoPort;
    @Mock
    private DiffSanitizer diffSanitizer;

    private CommandGateway gateway;

    @BeforeEach
    void setUp() {
        lenient().when(systemPromptProvider.getSystemPrompt(any())).thenReturn("");
        lenient().when(directivePort.getCombinedDirectives()).thenReturn("");
        lenient().when(gitRepoPort.getRawDiff(anyInt())).thenReturn("");
        lenient().when(diffSanitizer.sanitize(anyString())).thenAnswer(inv -> inv.getArgument(0));
        gateway = new CommandGateway(
                llmStreamPort,
                systemPromptProvider,
                directivePort,
                gitRepoPort,
                diffSanitizer,
                60_000L);
    }

    private static JsonRpcCommandRequest request(String input) {
        return new JsonRpcCommandRequest(
                "2.0",
                1,
                "process_voice",
                new JsonRpcCommandRequest.Params(input, false, null, null));
    }

    @Test
    void runStream_reviewPrOneOneFiveTen_fetchesPr11510() {
        gateway.runStream(request("review PR one one five ten"));
        verify(gitRepoPort).getRawDiff(11510);
    }

    @Test
    void runStream_reviewPr11510_fetchesPr11510() {
        gateway.runStream(request("review PR 1 1 5 10"));
        verify(gitRepoPort).getRawDiff(11510);
    }

    @Test
    void runStream_analyzePullRequestOneTwoThree_fetchesPr123() {
        gateway.runStream(request("analyze pull request one-two-three"));
        verify(gitRepoPort).getRawDiff(123);
    }

    @Test
    void runStream_reviewPrHash7_fetchesPr7() {
        gateway.runStream(request("review PR #7"));
        verify(gitRepoPort).getRawDiff(7);
    }

    @Test
    void runStream_reviewPrTen_fetchesPr10() {
        gateway.runStream(request("review pr ten"));
        verify(gitRepoPort).getRawDiff(10);
    }

    @Test
    void runStream_nonPrIntent_doesNotFetchDiff() {
        gateway.runStream(request("hello world"));
        verify(gitRepoPort, never()).getRawDiff(anyInt());
    }

    @Test
    void runStream_reviewPrNumberFive_fetchesPr5() {
        gateway.runStream(request("review pull request number five"));
        verify(gitRepoPort).getRawDiff(5);
    }
}
