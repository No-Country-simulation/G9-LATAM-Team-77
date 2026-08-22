package com.financeia.financeia_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void shouldUseLocalFallbackForBlankConfiguration() {
        CorsConfiguration configuration = configurationFor("   ");

        assertEquals(List.of("http://localhost:4321"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowCredentials());
    }

    @Test
    void shouldAcceptCommaSeparatedProductionOrigins() {
        CorsConfiguration configuration = configurationFor(
                "https://app.financeai.example, https://admin.financeai.example"
        );

        assertEquals(
                List.of("https://app.financeai.example", "https://admin.financeai.example"),
                configuration.getAllowedOrigins()
        );
    }

    @Test
    void shouldRejectWildcardOriginWhenCredentialsAreEnabled() {
        assertThrows(
                IllegalArgumentException.class,
                () -> corsConfig.corsConfigurationSource("*")
        );
    }

    private CorsConfiguration configurationFor(String origins) {
        CorsConfigurationSource source = corsConfig.corsConfigurationSource(origins);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/health");
        return source.getCorsConfiguration(request);
    }
}
