package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

public record FieldComparisonDto(String field, String formValue, String documentValue, boolean match) {
}
