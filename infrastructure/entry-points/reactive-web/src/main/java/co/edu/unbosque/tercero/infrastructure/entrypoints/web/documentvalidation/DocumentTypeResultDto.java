package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

public record DocumentTypeResultDto(String expected, String detected, boolean match) {
}
