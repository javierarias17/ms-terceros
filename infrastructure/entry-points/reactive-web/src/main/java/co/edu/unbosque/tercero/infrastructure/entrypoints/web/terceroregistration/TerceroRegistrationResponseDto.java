package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import io.swagger.v3.oas.annotations.media.Schema;

public record TerceroRegistrationResponseDto(

        @Schema(description = "Id generado para el registro.", example = "3f2e1a9c-...")
        String id,

        @Schema(description = "Estado del registro recién creado.", example = "REGISTRADO", allowableValues = {"REGISTRADO", "APROBADO", "RECHAZADO"})
        String estado) {
}
