package com.argos.adapter.rest;

import com.argos.domain.model.ArgosCommand;
import com.argos.domain.ports.IntelligencePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Command Hub. HTTP/JSON only; delegates to IntelligencePort.
 */
@RestController
@RequestMapping("/api/v1")
public class KernelController {

    private final IntelligencePort intelligencePort;

    public KernelController(IntelligencePort intelligencePort) {
        this.intelligencePort = intelligencePort;
    }

    @PostMapping("/command")
    public ResponseEntity<AgentResponse> command(@RequestBody CommandRequest request) {
        ArgosCommand command = new ArgosCommand(request.input() != null ? request.input() : "");
        AgentResponse response = intelligencePort.process(command);
        return ResponseEntity.ok(response);
    }

    /**
     * Request body for POST /api/v1/command.
     */
    public record CommandRequest(String input) {
    }
}
