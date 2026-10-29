package co.edu.unbosque.tercero.infrastructure.adapters.sendgrid.dto;

import java.util.List;

/**
 * Cuerpo del endpoint {@code POST /v3/mail/send} de SendGrid. Ver
 * <a href="https://www.twilio.com/docs/sendgrid/api-reference/mail-send/mail-send">referencia</a>.
 */
public record SendGridMailRequestDto(
        List<Personalization> personalizations,
        EmailAddressDto from,
        String subject,
        List<ContentDto> content) {

    public record Personalization(List<EmailAddressDto> to) {
    }

    public record EmailAddressDto(String email, String name) {
    }

    public record ContentDto(String type, String value) {
    }
}
