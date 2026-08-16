package com.financeia.financeia_backend.entity;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public enum Role {

    USER,
    ADMIN;

    public List<SimpleGrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + this.name())
        );
    }
}