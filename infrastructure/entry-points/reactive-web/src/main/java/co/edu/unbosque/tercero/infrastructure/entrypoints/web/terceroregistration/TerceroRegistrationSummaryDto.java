package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import io.swagger.v3.oas.annotations.media.Schema;

public record TerceroRegistrationSummaryDto(

        String id,

        @Schema(example = "Cliente", allowableValues = {"Cliente", "Proveedor"})
        String calidad,

        @Schema(example = "Natural", allowableValues = {"Natural", "Juridica"})
        String tipoPersona,

        String nombre,

        @Schema(description = "Cédula, o NIT si la solicitud es de persona jurídica.")
        String numeroIdentificacion,

        @Schema(allowableValues = {"REGISTRADO", "APROBADO", "RECHAZADO"})
        String estado,

        String fechaRegistro) {
}
