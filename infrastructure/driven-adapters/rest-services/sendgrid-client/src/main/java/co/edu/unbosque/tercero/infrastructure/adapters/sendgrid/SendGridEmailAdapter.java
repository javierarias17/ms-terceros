package co.edu.unbosque.tercero.infrastructure.adapters.sendgrid;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.model.exception.BusinessException;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import co.edu.unbosque.tercero.infrastructure.adapters.sendgrid.config.SendGridProperties;
import co.edu.unbosque.tercero.infrastructure.adapters.sendgrid.dto.SendGridMailRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Sends the registration confirmation email through SendGrid's {@code /v3/mail/send} API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SendGridEmailAdapter implements IEmailNotificationGateway {

    private static final String SEND_PATH = "/v3/mail/send";

    private final WebClient sendGridWebClient;
    private final SendGridProperties properties;

    @Override
    public Mono<Void> sendRegistrationConfirmation(RegistrationNotificationRequest request) {
        return sendGridWebClient.post()
                .uri(SEND_PATH)
                .bodyValue(buildMailRequest(request))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(errorBody -> {
                            log.error("SendGrid rechazó el correo para {}: {} {}",
                                    request.getRecipientEmail(), response.statusCode(), errorBody);
                            return Mono.error(new BusinessException(
                                    "EMAIL_SEND_FAILED", "No se pudo enviar el correo de confirmación"));
                        }))
                .toBodilessEntity()
                .then();
    }

    private SendGridMailRequestDto buildMailRequest(RegistrationNotificationRequest request) {
        var to = new SendGridMailRequestDto.EmailAddressDto(
                request.getRecipientEmail(), request.getNombreCompleto());
        var from = new SendGridMailRequestDto.EmailAddressDto(
                properties.getFromEmail(), properties.getFromName());
        var personalization = new SendGridMailRequestDto.Personalization(List.of(to));
        var content = new SendGridMailRequestDto.ContentDto("text/html", buildHtmlBody(request));

        return new SendGridMailRequestDto(
                List.of(personalization), from, "Registro de tercero recibido", List.of(content));
    }

    private String buildHtmlBody(RegistrationNotificationRequest request) {
        return """
                <div style="font-family: Arial, sans-serif; color: #1f2937; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #166534;">Registro recibido</h2>
                  <p>Hola %s,</p>
                  <p>Confirmamos que tu registro como <strong>%s</strong> fue recibido correctamente.</p>
                  <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                    <tr><td style="padding: 6px 0; color: #6b7280;">Número de identificación</td><td style="padding: 6px 0; font-weight: 600;">%s</td></tr>
                  </table>
                  <p style="color: #6b7280; font-size: 12px;">Si no reconoces esta solicitud, ignora este correo.</p>
                </div>
                """.formatted(
                escapeHtml(request.getNombreCompleto()),
                escapeHtml(request.getTipoTercero()),
                escapeHtml(request.getNumeroIdentificacion()));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
