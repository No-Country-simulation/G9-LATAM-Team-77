package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.LoginRequest;
import com.financeia.financeia_backend.dto.auth.LoginResponse;
import com.financeia.financeia_backend.dto.auth.RegistroRequest;
import com.financeia.financeia_backend.dto.auth.RegistroResponse;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.entity.Role;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.exception.ApiException;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
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
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Pais pais = paisRepository.findById(request.paisId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "País no encontrado"));

        Moneda moneda = monedaRepository.findById(request.monedaId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Moneda no encontrada"));

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
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
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
