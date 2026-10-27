package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultado de validar un documento: si el tipo coincide, y si cada campo extraído coincide con el formulario.")
public record DocumentValidationResponseDto(

        @Schema(description = "Comparación entre el tipo de documento esperado y el detectado por el clasificador.")
        DocumentTypeResultDto documentType,

        @Schema(description = "Comparación campo por campo entre el formulario y el documento. Vacía si documentType.match es false (nunca se llegó a extraer nada).")
        List<FieldComparisonDto> fields,

        @Schema(description = "true solo si el tipo de documento coincide Y todos los campos en 'fields' coinciden.", example = "true")
        boolean valid) {
}
