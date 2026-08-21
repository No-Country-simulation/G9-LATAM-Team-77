package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.LoginRequest;
import com.financeia.financeia_backend.dto.auth.LoginResponse;
import com.financeia.financeia_backend.dto.auth.RegistroRequest;
import com.financeia.financeia_backend.dto.auth.RegistroResponse;
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
    public RegistroResponse register(RegistroRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("El correo ya está registrado");
        }

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
                // Si hubo condición de carrera y otro hilo lo insertó primero, lo recuperamos
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
}