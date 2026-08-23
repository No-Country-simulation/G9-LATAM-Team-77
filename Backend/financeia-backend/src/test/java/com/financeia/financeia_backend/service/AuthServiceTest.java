package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.auth.LoginRequest;
import com.financeia.financeia_backend.dto.auth.LoginResponse;
import com.financeia.financeia_backend.dto.auth.GoogleLoginRequest;
import com.financeia.financeia_backend.dto.auth.RegistroRequest;
import com.financeia.financeia_backend.dto.auth.RegistroResponse;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.entity.Role;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import com.financeia.financeia_backend.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;

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

    @Mock
    private GoogleIdentityService googleIdentityService;

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

    @Test
    void deberiaIniciarSesionConIdentidadGoogleYaVinculada() {
        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token", null, null);
        User user = new User();
        user.setId(10L);
        user.setName("Juan");
        user.setEmail("juan@gmail.com");
        user.setGoogleSubject("google-sub-123");

        when(googleIdentityService.verify("google-id-token")).thenReturn(
                new GoogleIdentityService.GoogleIdentity(
                        "google-sub-123", "juan@gmail.com", "Juan", true)
        );
        when(userRepository.findByGoogleSubject("google-sub-123")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("juan@gmail.com")).thenReturn("financeai-jwt");

        LoginResponse response = authService.googleLogin(request);

        assertEquals("financeai-jwt", response.token());
        assertEquals(10L, response.userId());
        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    void deberiaSolicitarPerfilParaCuentaGoogleNueva() {
        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token", null, null);
        when(googleIdentityService.verify("google-id-token")).thenReturn(
                new GoogleIdentityService.GoogleIdentity(
                        "google-sub-new", "nueva@gmail.com", "Nueva Persona", true)
        );
        when(userRepository.findByGoogleSubject("google-sub-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("nueva@gmail.com")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> authService.googleLogin(request));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deberiaCrearCuentaGoogleConPaisYMoneda() {
        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token", 1L, 2L);
        Pais pais = new Pais();
        pais.setId(1L);
        Moneda moneda = new Moneda();
        moneda.setId(2L);

        when(googleIdentityService.verify("google-id-token")).thenReturn(
                new GoogleIdentityService.GoogleIdentity(
                        "google-sub-new", "nueva@gmail.com", "Nueva Persona", true)
        );
        when(userRepository.findByGoogleSubject("google-sub-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("nueva@gmail.com")).thenReturn(Optional.empty());
        when(paisRepository.findById(1L)).thenReturn(Optional.of(pais));
        when(monedaRepository.findById(2L)).thenReturn(Optional.of(moneda));
        when(passwordEncoder.encode(anyString())).thenReturn("password-aleatoria-hasheada");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });
        when(jwtService.generateToken("nueva@gmail.com")).thenReturn("financeai-jwt");

        LoginResponse response = authService.googleLogin(request);

        assertEquals("financeai-jwt", response.token());
        assertEquals(20L, response.userId());
        verify(passwordEncoder).encode(argThat(password -> password.length() == 64));
        verify(userRepository).save(argThat(user ->
                "google-sub-new".equals(user.getGoogleSubject())
                        && user.getCountry() == pais
                        && user.getMoneda() == moneda
                        && user.getRole() == Role.USER
        ));
    }

    @Test
    void noDeberiaVincularCorreoExternoNoAutoritativo() {
        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token", null, null);
        User existing = new User();
        existing.setEmail("persona@example.com");

        when(googleIdentityService.verify("google-id-token")).thenReturn(
                new GoogleIdentityService.GoogleIdentity(
                        "google-sub-external", "persona@example.com", "Persona", false)
        );
        when(userRepository.findByGoogleSubject("google-sub-external")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("persona@example.com")).thenReturn(Optional.of(existing));

        ApiException exception = assertThrows(ApiException.class, () -> authService.googleLogin(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(anyString());
    }
}
