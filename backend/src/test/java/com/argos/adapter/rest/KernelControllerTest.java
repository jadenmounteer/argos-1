package com.argos.adapter.rest;

import com.argos.application.gateway.CommandGateway;
import com.argos.application.gateway.JsonRpcCommandRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KernelController.class)
class KernelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommandGateway commandGateway;

    @Test
    void command_postsJsonRpcBody_returnsSseStream() throws Exception {
        when(commandGateway.runStream(any(JsonRpcCommandRequest.class)))
                .thenReturn(new SseEmitter(0L));

        mockMvc.perform(post("/api/v1/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"command\",\"params\":{\"input\":\"hello\"}}"))
                .andExpect(status().isOk());
    }
}
