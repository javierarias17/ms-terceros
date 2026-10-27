package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldComparisonDto(

        @Schema(description = "Nombre del campo comparado.",
                allowableValues = {"numeroDocumento", "nombreCompleto", "tipoContribuyente"}, example = "numeroDocumento")
        String field,

        @Schema(description = "Valor tal como lo escribió el solicitante en el formulario.", example = "1020304050")
        String formValue,

        @Schema(description = "Valor extraído del documento por Azure Document Intelligence. null si el campo no se pudo extraer (confianza por debajo del umbral, o el campo no está en el documento).", example = "1020304050")
        String documentValue,

        @Schema(description = "true si formValue y documentValue coinciden (número exacto, o nombre por conjunto de palabras sin importar orden/acentos/mayúsculas).", example = "true")
        boolean match) {
}
