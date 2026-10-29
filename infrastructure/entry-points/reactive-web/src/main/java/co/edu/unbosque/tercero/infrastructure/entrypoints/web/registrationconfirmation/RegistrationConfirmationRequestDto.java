package co.edu.unbosque.tercero.infrastructure.entrypoints.web.registrationconfirmation;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrationConfirmationRequestDto(

        @Schema(description = "Correo al que se envía la confirmación.", example = "ana.gomez@unbosque.edu.co", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Nombre completo (persona natural) o razón social (persona jurídica) del tercero.", example = "Ana Gomez")
        String nombreCompleto,

        @Schema(description = "Calidad en la que se registra el tercero.", example = "Proveedor",
                allowableValues = {"Proveedor", "Cliente"})
        String tipoTercero,

        @Schema(description = "Cédula o NIT del tercero.", example = "1020304050")
        String numeroIdentificacion) {
}
