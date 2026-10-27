package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentTypeResultDto(

        @Schema(description = "El tipo de documento que se pidió validar (expectedDocumentType de la petición).",
                allowableValues = {"CEDULA", "RUT"}, example = "CEDULA")
        String expected,

        @Schema(description = "El tipo de documento que el clasificador de Azure realmente detectó en el archivo. Puede ser null si el clasificador no reconoció ningún tipo.",
                allowableValues = {"CEDULA", "RUT"}, example = "CEDULA")
        String detected,

        @Schema(description = "true si expected y detected son el mismo tipo.", example = "true")
        boolean match) {
}
