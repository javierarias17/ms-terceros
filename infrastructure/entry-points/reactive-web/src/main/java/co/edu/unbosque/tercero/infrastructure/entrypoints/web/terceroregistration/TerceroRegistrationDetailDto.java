package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TerceroRegistrationDetailDto(
        String id,
        String calidad,
        String nacionalidad,
        String tipoPersona,
        String correo,
        String nombre,
        String cedula,
        String fechaExpedicionCedula,
        String lugarNacimiento,
        String paisNacimiento,
        String nit,
        String celular,
        String direccion,

        @Schema(allowableValues = {"REGISTRADO", "APROBADO", "RECHAZADO"})
        String estado,

        @Schema(description = "Solo tiene valor cuando estado=RECHAZADO.")
        String motivoRechazo,

        String fechaRegistro,
        List<TerceroDocumentDto> documents) {
}
