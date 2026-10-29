package co.edu.unbosque.tercero.infrastructure.entrypoints.web.registrationconfirmation;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrationConfirmationResponseDto(

        @Schema(description = "true si SendGrid aceptó el correo para envío.", example = "true")
        boolean sent) {
}
