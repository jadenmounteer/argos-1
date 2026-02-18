package com.argos.adapter.rest;

import com.argos.domain.model.ArgosCommand;
import com.argos.domain.ports.IntelligencePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KernelController.class)
class KernelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IntelligencePort intelligencePort;

    @Test
    void command_postsInput_returnsAgentResponse() throws Exception {
        when(intelligencePort.process(any(ArgosCommand.class)))
                .thenReturn(new AgentResponse("", "", "Stub response"));

        mockMvc.perform(post("/api/v1/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\": \"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.thought").value(""))
                .andExpect(jsonPath("$.action").value(""))
                .andExpect(jsonPath("$.response").value("Stub response"));
    }
}
