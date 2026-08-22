package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.config.SecurityConfig;
import com.financeia.financeia_backend.dto.auth.LoginResponse;
import com.financeia.financeia_backend.exception.ApiException;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.service.AuthService;
import com.financeia.financeia_backend.service.JwtService;
import com.financeia.financeia_backend.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldAllowLoginWithoutPreviousAuthentication() throws Exception {
        when(authService.login(any())).thenReturn(
                new LoginResponse("jwt-token", 1L, "Ana", "ana@test.com")
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@test.com","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldReturnControlledUnauthorizedResponseForInvalidLogin() throws Exception {
        when(authService.login(any())).thenThrow(
                new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas")
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@test.com","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void shouldAllowForgotPasswordWithoutAuthentication() throws Exception {
        when(passwordResetService.requestPasswordReset(any())).thenReturn(
                new com.financeia.financeia_backend.dto.auth.MessageResponse(
                        PasswordResetService.FORGOT_PASSWORD_RESPONSE)
        );

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(PasswordResetService.FORGOT_PASSWORD_RESPONSE));
    }

    @Test
    void shouldAllowResetPasswordWithoutAuthentication() throws Exception {
        when(passwordResetService.resetPassword(any())).thenReturn(
                new com.financeia.financeia_backend.dto.auth.MessageResponse(
                        PasswordResetService.RESET_PASSWORD_RESPONSE)
        );

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"token-seguro","newPassword":"NuevaClave#2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(PasswordResetService.RESET_PASSWORD_RESPONSE));
    }
}
