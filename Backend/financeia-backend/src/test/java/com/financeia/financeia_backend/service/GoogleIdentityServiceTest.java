package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleIdentityServiceTest {

    @Test
    void shouldRejectGoogleLoginWhenClientIdIsNotConfigured() {
        GoogleIdentityService service = new GoogleIdentityService("");

        ApiException exception = assertThrows(
                ApiException.class,
                () -> service.verify("untrusted-token")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    @Test
    void shouldRejectMalformedGoogleCredentialAsUnauthorized() {
        GoogleIdentityService service = new GoogleIdentityService(
                "1234567890-financeai-test.apps.googleusercontent.com"
        );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> service.verify("not-a-jwt")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}
