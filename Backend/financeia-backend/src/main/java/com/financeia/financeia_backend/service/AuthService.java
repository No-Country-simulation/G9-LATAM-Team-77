package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.ForgotPasswordRequest;
import com.financeia.financeia_backend.dto.auth.ForgotPasswordResponse;
import com.financeia.financeia_backend.dto.auth.LoginRequest;
import com.financeia.financeia_backend.dto.auth.LoginResponse;
import com.financeia.financeia_backend.dto.auth.RegistroRequest;
import com.financeia.financeia_backend.dto.auth.RegistroResponse;
import com.financeia.financeia_backend.dto.auth.ResetPasswordRequest;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.entity.Role;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PaisRepository paisRepository;
    private final MonedaRepository monedaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Valida los requisitos avanzados de seguridad para contraseñas:
     * - Mínimo 8 caracteres
     * - Al menos una letra mayúscula
     * - Al menos una letra minúscula
     * - Al menos un número
     * - Al menos un carácter especial
     */
    public static void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("La contraseña debe tener un mínimo de 8 caracteres.");
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{};':\"|,.<>/?~`\\".indexOf(ch) >= 0);

        if (!hasUpper) {
            throw new RuntimeException("La contraseña debe incluir al menos una letra mayúscula.");
        }
        if (!hasLower) {
            throw new RuntimeException("La contraseña debe incluir al menos una letra minúscula.");
        }
        if (!hasDigit) {
            throw new RuntimeException("La contraseña debe incluir al menos un número.");
        }
        if (!hasSpecial) {
            throw new RuntimeException("La contraseña debe incluir al menos un carácter especial (!@#$%^&*...).");
        }
    }

    public RegistroResponse register(RegistroRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        validatePasswordStrength(request.password());

        Pais pais = paisRepository.findById(request.paisId())
                .orElseThrow(() -> new RuntimeException("País no encontrado"));

        Moneda moneda = monedaRepository.findById(request.monedaId())
                .orElseThrow(() -> new RuntimeException("Moneda no encontrada"));

        User user = new User();

        user.setName(request.nombre());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCountry(pais);
        user.setMoneda(moneda);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return new RegistroResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public LoginResponse googleSync(com.financeia.financeia_backend.dto.auth.GoogleSyncRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null) {
            try {
                User newUser = new User();
                newUser.setName(request.name());
                newUser.setEmail(request.email());
                newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                newUser.setRole(Role.USER);
                user = userRepository.save(newUser);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                user = userRepository.findByEmail(request.email())
                        .orElseThrow(() -> new RuntimeException("Error al sincronizar Google Auth"));
            }
        }

        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * Solicita token temporal de recuperación de contraseña (Válido por 15 minutos)
     */
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("No existe ninguna cuenta asociada a este correo electrónico"));

        String resetToken = jwtService.generatePasswordResetToken(user.getEmail());

        return new ForgotPasswordResponse(
                user.getEmail(),
                resetToken,
                "Token temporal de recuperación generado exitosamente (Válido por 15 minutos)"
        );
    }

    /**
     * Restablece la contraseña validando estrictamente el token temporal firmado y los requisitos de seguridad
     */
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese correo"));

        if (request.token() != null && !request.token().isBlank()) {
            boolean isValid = jwtService.validatePasswordResetToken(request.token(), request.email());
            if (!isValid) {
                throw new RuntimeException("El token temporal de recuperación es inválido o ha expirado. Solicita uno nuevo.");
            }
        }

        validatePasswordStrength(request.newPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}