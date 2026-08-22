package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.auth.LoginRequest;
import com.financeia.financeia_backend.dto.auth.LoginResponse;
import com.financeia.financeia_backend.dto.auth.RegistroRequest;
import com.financeia.financeia_backend.dto.auth.RegistroResponse;
import com.financeia.financeia_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authService);
    }


    @Test
    void deberiaRegistrarUsuario() {

        RegistroRequest request = new RegistroRequest(
                "Juan",
                "juan@gmail.com",
                "123456",
                1L,
                1L
        );

        RegistroResponse response = new RegistroResponse(
                1L,
                "Juan",
                "juan@gmail.com"
        );

        when(authService.register(any(RegistroRequest.class)))
                .thenReturn(response);

        ResponseEntity<RegistroResponse> result =
                authController.register(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());

        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().id());
        assertEquals("Juan", result.getBody().nombre());
        assertEquals("juan@gmail.com", result.getBody().email());
    }


    @Test
    void deberiaIniciarSesion() {

        LoginRequest request = new LoginRequest(
                "juan@gmail.com",
                "123456"
        );

        LoginResponse response = new LoginResponse(
                "jwt-token",
                1L,
                "Juan",
                "juan@gmail.com"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        ResponseEntity<LoginResponse> result =
                authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        assertNotNull(result.getBody());
        assertEquals("jwt-token", result.getBody().token());
        assertEquals(1L, result.getBody().userId());
        assertEquals("Juan", result.getBody().nombre());
        assertEquals("juan@gmail.com", result.getBody().email());
    }


    @Test
    void deberiaLlamarAlServicioAlRegistrar() {

        RegistroRequest request = new RegistroRequest(
                "Juan",
                "juan@gmail.com",
                "123456",
                1L,
                1L
        );

        RegistroResponse response = new RegistroResponse(
                1L,
                "Juan",
                "juan@gmail.com"
        );

        when(authService.register(any(RegistroRequest.class)))
                .thenReturn(response);

        authController.register(request);

        org.mockito.Mockito.verify(authService)
                .register(request);
    }


    @Test
    void deberiaLlamarAlServicioAlIniciarSesion() {

        LoginRequest request = new LoginRequest(
                "juan@gmail.com",
                "123456"
        );

        LoginResponse response = new LoginResponse(
                "jwt-token",
                1L,
                "Juan",
                "juan@gmail.com"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        authController.login(request);

        org.mockito.Mockito.verify(authService)
                .login(request);
    }
}