package co.edu.unbosque.tercero.infrastructure.adapters.sendgrid;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
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
 * Sends transactional emails through SendGrid's {@code /v3/mail/send} API.
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
        return sendEmail(
                request.getRecipientEmail(),
                request.getNombreCompleto(),
                "Registro de tercero recibido",
                buildConfirmationHtmlBody(request));
    }

    @Override
    public Mono<Void> sendStatusUpdate(TerceroRegistration registration) {
        boolean aprobado = registration.getEstado() == RegistrationStatus.APROBADO;
        String subject = aprobado ? "Tu registro de tercero fue aprobado" : "Tu registro de tercero fue rechazado";

        return sendEmail(
                registration.getCorreo(),
                registration.getNombre(),
                subject,
                buildStatusUpdateHtmlBody(registration, aprobado));
    }

    private Mono<Void> sendEmail(String toEmail, String toName, String subject, String htmlBody) {
        var to = new SendGridMailRequestDto.EmailAddressDto(toEmail, toName);
        var from = new SendGridMailRequestDto.EmailAddressDto(properties.getFromEmail(), properties.getFromName());
        var personalization = new SendGridMailRequestDto.Personalization(List.of(to));
        var content = new SendGridMailRequestDto.ContentDto("text/html", htmlBody);
        var mailRequest = new SendGridMailRequestDto(List.of(personalization), from, subject, List.of(content));

        return sendGridWebClient.post()
                .uri(SEND_PATH)
                .bodyValue(mailRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(errorBody -> {
                            log.error("SendGrid rechazó el correo para {}: {} {}", toEmail, response.statusCode(), errorBody);
                            return Mono.error(new BusinessException("EMAIL_SEND_FAILED", "No se pudo enviar el correo"));
                        }))
                .toBodilessEntity()
                .then();
    }

    private String buildConfirmationHtmlBody(RegistrationNotificationRequest request) {
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

    private String buildStatusUpdateHtmlBody(TerceroRegistration registration, boolean aprobado) {
        String colorTitulo = aprobado ? "#166534" : "#991b1b";
        String tituloMensaje = aprobado ? "Registro aprobado" : "Registro rechazado";
        String cuerpoMensaje = aprobado
                ? "Tu registro como <strong>%s</strong> fue revisado y aprobado. Ya quedaste habilitado ante la Universidad El Bosque."
                        .formatted(escapeHtml(registration.getCalidad()))
                : "Tu registro como <strong>%s</strong> fue revisado y no fue aprobado."
                        .formatted(escapeHtml(registration.getCalidad()));
        String motivoHtml = (!aprobado && registration.getMotivoRechazo() != null && !registration.getMotivoRechazo().isBlank())
                ? """
                        <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                          <tr><td style="padding: 6px 0; color: #6b7280;">Motivo</td><td style="padding: 6px 0; font-weight: 600;">%s</td></tr>
                        </table>
                        """.formatted(escapeHtml(registration.getMotivoRechazo()))
                : "";

        return """
                <div style="font-family: Arial, sans-serif; color: #1f2937; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: %s;">%s</h2>
                  <p>Hola %s,</p>
                  <p>%s</p>
                  %s
                  <p style="color: #6b7280; font-size: 12px;">Si tienes preguntas, responde directamente a este correo.</p>
                </div>
                """.formatted(
                colorTitulo,
                tituloMensaje,
                escapeHtml(registration.getNombre()),
                cuerpoMensaje,
                motivoHtml);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
