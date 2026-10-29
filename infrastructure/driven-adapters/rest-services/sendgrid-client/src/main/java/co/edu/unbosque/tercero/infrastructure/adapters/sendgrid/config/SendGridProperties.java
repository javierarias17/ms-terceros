package co.edu.unbosque.tercero.infrastructure.adapters.sendgrid.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the SendGrid account (API key, sender identity) from {@code sendgrid.*} in
 * application.yml. {@code fromEmail} debe ser un remitente verificado en SendGrid (Settings >
 * Sender Authentication), o el envío falla.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sendgrid")
public class SendGridProperties {

    private String apiKey;
    private String fromEmail;
    private String fromName = "Universidad El Bosque";
}
