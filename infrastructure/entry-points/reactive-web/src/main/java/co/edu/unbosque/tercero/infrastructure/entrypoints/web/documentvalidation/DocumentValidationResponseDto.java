package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

import java.util.List;

public record DocumentValidationResponseDto(
        DocumentTypeResultDto documentType,
        List<FieldComparisonDto> fields,
        boolean valid) {
}
