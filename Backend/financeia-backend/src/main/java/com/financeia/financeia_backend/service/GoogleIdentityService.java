package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.exception.ApiException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Locale;

@Service
public class GoogleIdentityService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityService(
            @Value("${financeai.google.client-id:}") String clientId
    ) {
        String normalizedClientId = clientId == null ? "" : clientId.trim();
        if (normalizedClientId.isEmpty()) {
            this.verifier = null;
            return;
        }

        try {
            this.verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(normalizedClientId))
                    .build();
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("No fue posible inicializar la validación de Google", ex);
        }
    }

    public GoogleIdentity verify(String credential) {
        if (verifier == null) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El inicio de sesión con Google no está configurado"
            );
        }

        try {
            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw invalidCredential();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String subject = normalized(payload.getSubject());
            String email = normalized(payload.getEmail()).toLowerCase(Locale.ROOT);
            String name = normalized((String) payload.get("name"));
            String hostedDomain = normalized(payload.getHostedDomain());

            if (subject.isEmpty() || email.isEmpty() || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw invalidCredential();
            }

            boolean authoritativeEmail = email.endsWith("@gmail.com") || !hostedDomain.isEmpty();
            return new GoogleIdentity(
                    subject,
                    email,
                    name.isEmpty() ? "Usuario Google" : truncate(name, 255),
                    authoritativeEmail
            );
        } catch (GeneralSecurityException | IOException | IllegalArgumentException ex) {
            throw invalidCredential();
        }
    }

    private static ApiException invalidCredential() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "La credencial de Google no es válida");
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim();
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record GoogleIdentity(
            String subject,
            String email,
            String name,
            boolean authoritativeEmail
    ) {
    }
}
