package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.ForgotPasswordRequest;
import com.financeia.financeia_backend.dto.auth.LoginRequest;
import com.financeia.financeia_backend.dto.auth.ResetPasswordRequest;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.entity.Role;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.exception.ApiException;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class PasswordResetFlowIntegrationTest {

    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaisRepository paisRepository;
    @Autowired
    private MonedaRepository monedaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private MailService mailService;

    @Test
    void validTokenChangesPasswordAndCannotBeReused() {
        String email = "reset-flow@test.com";
        String oldPassword = "Anterior#2026";
        String newPassword = "NuevaClave#2026";
        Pais country = paisRepository.save(new Pais(null, "País Reset", "PR"));
        Moneda currency = monedaRepository.save(new Moneda(null, "Moneda Reset", "MRT", "R"));

        User user = new User();
        user.setName("Usuario Reset");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(oldPassword));
        user.setCountry(country);
        user.setMoneda(currency);
        user.setRole(Role.USER);
        userRepository.saveAndFlush(user);

        passwordResetService.requestPasswordReset(new ForgotPasswordRequest(email));

        var tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mailService).sendPasswordResetEmail(eq(user), tokenCaptor.capture(), eq(30L));
        String rawToken = tokenCaptor.getValue();

        passwordResetService.resetPassword(new ResetPasswordRequest(rawToken, newPassword));

        assertThatThrownBy(() -> authService.login(new LoginRequest(email, oldPassword)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Credenciales inválidas");
        assertThat(authService.login(new LoginRequest(email, newPassword)).email()).isEqualTo(email);
        assertThatThrownBy(() -> passwordResetService.resetPassword(
                new ResetPasswordRequest(rawToken, "OtraClave#2026")))
                .isInstanceOf(ApiException.class)
                .hasMessage("El enlace de recuperación no es válido o ya fue utilizado.");
    }
}
