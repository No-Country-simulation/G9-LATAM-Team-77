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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaisRepository paisRepository;

    @Mock
    private MonedaRepository monedaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;


    // =========================
    // REGISTRO
    // =========================

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {

        RegistroRequest request = new RegistroRequest(
                "Juan",
                "juan@gmail.com",
                "123456",
                1L,
                1L
        );

        Pais pais = new Pais();
        pais.setId(1L);

        Moneda moneda = new Moneda();
        moneda.setId(1L);

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setName("Juan");
        savedUser.setEmail("juan@gmail.com");

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(paisRepository.findById(request.paisId()))
                .thenReturn(Optional.of(pais));

        when(monedaRepository.findById(request.monedaId()))
                .thenReturn(Optional.of(moneda));

        when(passwordEncoder.encode(request.password()))
                .thenReturn("password-encriptada");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegistroResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Juan", response.nombre());
        assertEquals("juan@gmail.com", response.email());

        verify(userRepository).existsByEmail("juan@gmail.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void noDeberiaRegistrarCorreoDuplicado() {

        RegistroRequest request = new RegistroRequest(
                "Juan",
                "juan@gmail.com",
                "123456",
                1L,
                1L
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "El correo ya está registrado",
                exception.getMessage()
        );

        verify(userRepository).existsByEmail("juan@gmail.com");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }


    @Test
    void noDeberiaRegistrarSiElPaisNoExiste() {

        RegistroRequest request = new RegistroRequest(
                "Juan",
                "juan@gmail.com",
                "123456",
                99L,
                1L
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(paisRepository.findById(request.paisId()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "País no encontrado",
                exception.getMessage()
        );

        verify(paisRepository).findById(99L);
        verify(monedaRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void noDeberiaRegistrarSiLaMonedaNoExiste() {

        RegistroRequest request = new RegistroRequest(
                "Juan",
                "juan@gmail.com",
                "123456",
                1L,
                99L
        );

        Pais pais = new Pais();
        pais.setId(1L);

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(paisRepository.findById(request.paisId()))
                .thenReturn(Optional.of(pais));

        when(monedaRepository.findById(request.monedaId()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "Moneda no encontrada",
                exception.getMessage()
        );

        verify(monedaRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }


    // =========================
    // LOGIN
    // =========================

    @Test
    void deberiaIniciarSesionCorrectamente() {

        LoginRequest request = new LoginRequest(
                "juan@gmail.com",
                "123456"
        );

        User user = new User();
        user.setId(10L);
        user.setName("Juan");
        user.setEmail("juan@gmail.com");
        user.setPassword("password-encriptada");
        user.setRole(Role.USER);

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateToken(user.getEmail()))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(10L, response.userId());
        assertEquals("Juan", response.nombre());
        assertEquals("juan@gmail.com", response.email());

        verify(userRepository).findByEmail("juan@gmail.com");

        verify(passwordEncoder).matches(
                "123456",
                "password-encriptada"
        );

        verify(jwtService).generateToken("juan@gmail.com");
    }


    @Test
    void noDeberiaIniciarSesionConCorreoInexistente() {

        LoginRequest request = new LoginRequest(
                "juan@gmail.com",
                "123456"
        );

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(anyString());
    }


    @Test
    void noDeberiaIniciarSesionConPasswordIncorrecta() {

        LoginRequest request = new LoginRequest(
                "juan@gmail.com",
                "password-incorrecta"
        );

        User user = new User();
        user.setId(10L);
        user.setEmail("juan@gmail.com");
        user.setPassword("password-encriptada");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );

        verify(jwtService, never())
                .generateToken(anyString());
    }
}