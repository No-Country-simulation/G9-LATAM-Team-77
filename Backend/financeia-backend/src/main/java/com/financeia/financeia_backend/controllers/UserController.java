package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.user.UserResponse;
import com.financeia.financeia_backend.dto.user.UserUpdateRequest;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal User user
    ) {

        return ResponseEntity.ok(
                userService.getProfile(user)
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserUpdateRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateProfile(user, request)
        );
    }

    @DeleteMapping("/profile")
    public ResponseEntity<java.util.Map<String, String>> deleteAccount(
            @AuthenticationPrincipal User user
    ) {
        userService.deleteAccount(user);
        return ResponseEntity.ok(
                java.util.Map.of("message", "Cuenta y todos sus registros eliminados exitosamente de la base de datos")
        );
    }
}