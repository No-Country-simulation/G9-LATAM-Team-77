package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.ForgotPasswordRequest;
import com.financeia.financeia_backend.dto.auth.MessageResponse;
import com.financeia.financeia_backend.dto.auth.ResetPasswordRequest;
import com.financeia.financeia_backend.entity.PasswordResetToken;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.exception.ApiException;
import com.financeia.financeia_backend.repository.PasswordResetTokenRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    public static final String FORGOT_PASSWORD_RESPONSE =
            "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.";
    public static final String RESET_PASSWORD_RESPONSE =
            "Contraseña actualizada correctamente. Ya puedes iniciar sesión.";

    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureRandom secureRandom;
    private final Map<String, Instant> recentRequests = new ConcurrentHashMap<>();
    private final long expirationMinutes;
    private final long cooldownSeconds;

    @Autowired
    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            @Value("${financeai.password-reset.expiration-minutes:30}") long expirationMinutes,
            @Value("${financeai.password-reset.cooldown-seconds:60}") long cooldownSeconds
    ) {
        this(userRepository, tokenRepository, passwordEncoder, mailService,
                expirationMinutes, cooldownSeconds, new SecureRandom());
    }

    PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            long expirationMinutes,
            long cooldownSeconds,
            SecureRandom secureRandom
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.expirationMinutes = expirationMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (isInCooldown(normalizedEmail)) {
            return new MessageResponse(FORGOT_PASSWORD_RESPONSE);
        }

        userRepository.findByEmail(normalizedEmail).ifPresent(user -> createAndSendToken(user));
        return new MessageResponse(FORGOT_PASSWORD_RESPONSE);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Instant now = Instant.now();
        PasswordResetToken token = tokenRepository.findByTokenHashForUpdate(hashToken(request.token()))
                .orElseThrow(() -> invalidToken("El enlace de recuperación no es válido o ya fue utilizado."));

        if (token.isUsed()) {
            throw invalidToken("El enlace de recuperación no es válido o ya fue utilizado.");
        }
        if (!token.getExpiresAt().isAfter(now)) {
            token.setUsed(true);
            throw invalidToken("El enlace de recuperación expiró. Solicita uno nuevo.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        token.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(token);
        tokenRepository.invalidateActiveTokens(user.getId());

        return new MessageResponse(RESET_PASSWORD_RESPONSE);
    }

    private void createAndSendToken(User user) {
        tokenRepository.invalidateActiveTokens(user.getId());

        String rawToken = generateRawToken();
        Instant now = Instant.now();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES));
        token.setUsed(false);
        tokenRepository.save(token);

        mailService.sendPasswordResetEmail(user, rawToken, expirationMinutes);
    }

    private boolean isInCooldown(String normalizedEmail) {
        Instant now = Instant.now();
        String emailKey = hashToken(normalizedEmail);
        Instant previous = recentRequests.put(emailKey, now);
        if (recentRequests.size() > 10_000) {
            recentRequests.entrySet().removeIf(entry ->
                    entry.getValue().plus(cooldownSeconds, ChronoUnit.SECONDS).isBefore(now));
        }
        return previous != null && previous.plus(cooldownSeconds, ChronoUnit.SECONDS).isAfter(now);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private ApiException invalidToken(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
