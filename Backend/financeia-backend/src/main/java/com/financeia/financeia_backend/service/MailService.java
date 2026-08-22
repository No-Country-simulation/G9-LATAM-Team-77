package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final String SUBJECT = "FinanceAI - Recuperación de contraseña";
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${financeai.mail.from:no-reply@financeai.local}")
    private String from;

    @Value("${financeai.frontend.url:http://localhost:4321}")
    private String frontendUrl;

    @Async
    public void sendPasswordResetEmail(User user, String rawToken, long expirationMinutes) {
        try {
            String baseUrl = frontendUrl.endsWith("/")
                    ? frontendUrl.substring(0, frontendUrl.length() - 1)
                    : frontendUrl;
            String resetUrl = UriComponentsBuilder
                    .fromUriString(baseUrl + "/reset-password")
                    .queryParam("token", rawToken)
                    .build()
                    .toUriString();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject(SUBJECT);
            message.setText("Hola " + user.getName() + ",\n\n"
                    + "Recibimos una solicitud para restablecer tu contraseña de FinanceAI.\n\n"
                    + "Usa el siguiente enlace:\n" + resetUrl + "\n\n"
                    + "Este enlace expirará en " + expirationMinutes + " minutos.\n\n"
                    + "Si no solicitaste este cambio, puedes ignorar este mensaje.");

            mailSender.send(message);
        } catch (RuntimeException exception) {
            LOGGER.warn("No fue posible enviar un correo de recuperación para el usuario id={}", user.getId());
        }
    }
}
