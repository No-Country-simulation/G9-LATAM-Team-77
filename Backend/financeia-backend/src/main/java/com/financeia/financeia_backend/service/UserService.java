package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.user.UserResponse;
import com.financeia.financeia_backend.dto.user.UserUpdateRequest;
import com.financeia.financeia_backend.entity.Moneda;
import com.financeia.financeia_backend.entity.Pais;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.MonedaRepository;
import com.financeia.financeia_backend.repository.PaisRepository;
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

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCountry().getId(),
                user.getCountry().getNombre(),
                user.getMoneda().getId(),
                user.getMoneda().getNombre(),
                user.getMoneda().getCodigo(),
                user.getMoneda().getSimbolo()
        );
    }
}