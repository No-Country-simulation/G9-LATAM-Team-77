package com.financeia.financeia_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${financeai.cors.allowed-origins:http://localhost:4321}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = List.of(allowedOrigins.split(","))
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        if (origins.isEmpty()) {
            origins = List.of("http://localhost:4321");
        }

        if (origins.contains("*")) {
            throw new IllegalArgumentException(
                    "CORS no permite el origen comodín cuando las credenciales están habilitadas"
            );
        }

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
