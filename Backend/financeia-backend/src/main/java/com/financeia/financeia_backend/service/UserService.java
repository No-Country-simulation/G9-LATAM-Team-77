package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.user.UserResponse;
import com.financeia.financeia_backend.dto.user.UserUpdateRequest;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.HistorialAnalisisRepository;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
import com.financeia.financeia_backend.repository.TransactionRepository;
import com.financeia.financeia_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaisRepository paisRepository;
    private final MonedaRepository monedaRepository;
    private final TransactionRepository transactionRepository;
    private final HistorialAnalisisRepository historialAnalisisRepository;

    @Transactional
    public UserResponse getProfile(User user) {

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        return toResponse(currentUser);
    }

    @Transactional
    public UserResponse updateProfile(
            User user,
            UserUpdateRequest request
    ) {

        Pais pais = paisRepository.findById(request.paisId())
                .orElseThrow(() ->
                        new RuntimeException("País no encontrado"));

        Moneda moneda = monedaRepository.findById(request.monedaId())
                .orElseThrow(() ->
                        new RuntimeException("Moneda no encontrada"));

        user.setCountry(pais);
        user.setMoneda(moneda);

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    @Transactional
    public void deleteAccount(User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        // 1. Eliminar todas las transacciones vinculadas al usuario
        transactionRepository.deleteByUser(currentUser);

        // 2. Eliminar todo el historial de análisis vinculados al usuario
        historialAnalisisRepository.deleteByUsuarioId(currentUser.getId());

        // 3. Eliminar el usuario de la base de datos
        userRepository.delete(currentUser);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCountry() != null ? user.getCountry().getId() : null,
                user.getCountry() != null ? user.getCountry().getNombre() : null,
                user.getMoneda() != null ? user.getMoneda().getId() : null,
                user.getMoneda() != null ? user.getMoneda().getNombre() : null,
                user.getMoneda() != null ? user.getMoneda().getCodigo() : null,
                user.getMoneda() != null ? user.getMoneda().getSimbolo() : null
        );
    }
}