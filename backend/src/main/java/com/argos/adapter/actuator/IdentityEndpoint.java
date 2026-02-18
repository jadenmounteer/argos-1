package com.argos.adapter.actuator;

import com.argos.domain.ports.IntelligencePort;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Actuator endpoint for sanity check: verifies the LLM identifies as ARGOS-1.
 * GET /actuator/identity returns identityConfirmed and status for monitoring.
 */
@Component
@Endpoint(id = "identity")
public class IdentityEndpoint {

    private final IntelligencePort intelligencePort;

    public IdentityEndpoint(IntelligencePort intelligencePort) {
        this.intelligencePort = intelligencePort;
    }

    @ReadOperation
    public Map<String, Object> identity() {
        boolean confirmed = intelligencePort.verifyIdentity();
        return Map.of(
                "identityConfirmed", confirmed,
                "status", confirmed ? "UP" : "DOWN"
        );
    }
}
