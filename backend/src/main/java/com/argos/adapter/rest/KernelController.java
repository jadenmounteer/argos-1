package com.argos.adapter.rest;

import com.argos.application.gateway.CommandGateway;
import com.argos.application.gateway.JsonRpcCommandRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST adapter for the Command Hub. POST /api/v1/command accepts JSON-RPC body and returns SSE stream.
 */
@RestController
@RequestMapping("/api/v1")
public class KernelController {

    private final CommandGateway commandGateway;

    public KernelController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping(value = "/command", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter command(@RequestBody JsonRpcCommandRequest request) {
        return commandGateway.runStream(request);
    }
}
