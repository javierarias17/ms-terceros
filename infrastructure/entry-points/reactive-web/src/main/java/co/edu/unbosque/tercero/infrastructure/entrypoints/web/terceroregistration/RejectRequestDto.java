package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import io.swagger.v3.oas.annotations.media.Schema;

public record RejectRequestDto(

        @Schema(description = "Motivo del rechazo, se incluye en el correo al tercero.",
                example = "La cédula adjunta no coincide con el nombre del formulario.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String motivo) {
}
