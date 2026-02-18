package com.argos.adapter.actuator;

import com.argos.domain.ports.IntelligencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityEndpointTest {

    @Mock
    private IntelligencePort intelligencePort;

    @Test
    void identity_whenVerifyIdentityReturnsTrue_returnsConfirmedAndUp() {
        when(intelligencePort.verifyIdentity()).thenReturn(true);

        IdentityEndpoint endpoint = new IdentityEndpoint(intelligencePort);
        Map<String, Object> result = endpoint.identity();

        assertThat(result.get("identityConfirmed")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("UP");
    }

    @Test
    void identity_whenVerifyIdentityReturnsFalse_returnsNotConfirmedAndDown() {
        when(intelligencePort.verifyIdentity()).thenReturn(false);

        IdentityEndpoint endpoint = new IdentityEndpoint(intelligencePort);
        Map<String, Object> result = endpoint.identity();

        assertThat(result.get("identityConfirmed")).isEqualTo(false);
        assertThat(result.get("status")).isEqualTo("DOWN");
    }
}
