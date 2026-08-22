package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.ForgotPasswordRequest;
import com.financeia.financeia_backend.dto.auth.MessageResponse;
import com.financeia.financeia_backend.dto.auth.ResetPasswordRequest;
import com.financeia.financeia_backend.entity.PasswordResetToken;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.exception.ApiException;
import com.financeia.financeia_backend.repository.PasswordResetTokenRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailService mailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                mailService,
                30,
                0,
                new SecureRandom()
        );
    }

    @Test
    void existingUserCreatesHashedTokenAndReturnsGenericResponse() {
        User user = user(1L, "ana@test.com");
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user));

        MessageResponse response = service.requestPasswordReset(
                new ForgotPasswordRequest(" ANA@TEST.COM "));

        ArgumentCaptor<PasswordResetToken> storedToken = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> rawToken = ArgumentCaptor.forClass(String.class);
        verify(tokenRepository).invalidateActiveTokens(1L);
        verify(tokenRepository).save(storedToken.capture());
        verify(mailService).sendPasswordResetEmail(eq(user), rawToken.capture(), eq(30L));

        assertThat(response.message()).isEqualTo(PasswordResetService.FORGOT_PASSWORD_RESPONSE);
        assertThat(rawToken.getValue()).hasSize(43);
        assertThat(storedToken.getValue().getTokenHash())
                .isNotEqualTo(rawToken.getValue())
                .isEqualTo(PasswordResetService.hashToken(rawToken.getValue()));
        assertThat(storedToken.getValue().getExpiresAt())
                .isAfter(storedToken.getValue().getCreatedAt());
        assertThat(storedToken.getValue().isUsed()).isFalse();
    }

    @Test
    void unknownUserReturnsExactlyTheSamePublicResponse() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        MessageResponse response = service.requestPasswordReset(
                new ForgotPasswordRequest("missing@test.com"));

        assertThat(response.message()).isEqualTo(PasswordResetService.FORGOT_PASSWORD_RESPONSE);
        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetEmail(any(), any(), anyLong());
    }

    @Test
    void invalidTokenIsRejected() {
        when(tokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(
                new ResetPasswordRequest("invalid-token", "NuevaClave#2026")))
                .isInstanceOf(ApiException.class)
                .hasMessage("El enlace de recuperación no es válido o ya fue utilizado.");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void expiredTokenIsRejected() {
        PasswordResetToken token = token(user(1L, "ana@test.com"), false, Instant.now().minusSeconds(1));
        when(tokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(
                new ResetPasswordRequest("expired-token", "NuevaClave#2026")))
                .isInstanceOf(ApiException.class)
                .hasMessage("El enlace de recuperación expiró. Solicita uno nuevo.");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void usedTokenIsRejected() {
        PasswordResetToken token = token(user(1L, "ana@test.com"), true, Instant.now().plusSeconds(60));
        when(tokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(
                new ResetPasswordRequest("used-token", "NuevaClave#2026")))
                .isInstanceOf(ApiException.class)
                .hasMessage("El enlace de recuperación no es válido o ya fue utilizado.");
        verify(passwordEncoder, never()).encode(any());
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("Ana Prueba");
        user.setEmail(email);
        return user;
    }

    private PasswordResetToken token(User user, boolean used, Instant expiresAt) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setUsed(used);
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(Instant.now().minusSeconds(60));
        token.setTokenHash("hash");
        return token;
    }
}
