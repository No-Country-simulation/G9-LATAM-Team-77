package com.financeia.financeia_backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldPreserveControlledBusinessMessageAndStatus() {
        ResponseEntity<Map<String, Object>> response = handler.handleApiException(
                new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciales inválidas", response.getBody().get("message"));
    }

    @Test
    void shouldNotExposeUnexpectedInternalMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpectedException(
                new RuntimeException("SQL error at C:\\secret\\database with password=hidden")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String message = String.valueOf(response.getBody().get("message"));
        assertFalse(message.contains("SQL"));
        assertFalse(message.contains("C:\\secret"));
        assertFalse(message.contains("password"));
    }
}
