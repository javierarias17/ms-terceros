package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import io.swagger.v3.oas.annotations.media.Schema;

public record TerceroDocumentDto(

        @Schema(description = "Clave interna del documento (la define el frontend).", example = "cli_cedula")
        String documentKey,

        @Schema(description = "Nombre original del archivo subido.", example = "cedula.pdf")
        String fileName,

        @Schema(description = "URL firmada (SAS) para ver/descargar el documento. Expira a los 15 minutos de generada.")
        String url,

        @Schema(description = "Fecha y hora en que se subió el documento.")
        String uploadedAt) {
}
