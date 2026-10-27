package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

import co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared.ApiErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import reactor.core.publisher.Mono;

/**
 * Contrato público (OpenAPI) del endpoint de validación de documentos, separado de
 * {@link DocumentValidationController} para que la implementación no quede contaminada con
 * anotaciones de documentación. SpringDoc lee las anotaciones de esta interfaz igual que si
 * estuvieran en el controlador, siempre que este la implemente con {@code @Override}.
 */
@Tag(name = "Validación de documentos", description = "Clasifica y extrae datos de una cédula o un RUT con Azure AI Document Intelligence, y los compara con lo que el solicitante escribió en el formulario.")
@RequestMapping("/v1/document-validations")
public interface DocumentValidationApi {

    @Operation(
            summary = "Validar un documento (cédula o RUT)",
            description = """
                    Sube un archivo PDF y lo valida en dos pasos:

                    1. **Clasificación**: confirma que el archivo realmente es del tipo esperado (`expectedDocumentType`). Si el documento detectado no coincide, la respuesta vuelve con `valid=false` y `fields` vacío, sin intentar extraer nada.
                    2. **Extracción y comparación**: si el tipo coincide, extrae los campos relevantes del documento y los compara contra los valores del formulario:
                       - Para `CEDULA`: compara `numeroDocumento` y `nombreCompleto`.
                       - Para `RUT`: compara el tipo de contribuyente esperado según `applicantType`.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "El documento se procesó (coincida o no). Revisa el campo `valid` y cada `FieldComparisonDto.match` para el detalle.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "Cédula válida", value = """
                                            {
                                              "documentType": { "expected": "CEDULA", "detected": "CEDULA", "match": true },
                                              "fields": [
                                                { "field": "numeroDocumento", "formValue": "1020304050", "documentValue": "1020304050", "match": true },
                                                { "field": "nombreCompleto", "formValue": "Juan Perez Gomez", "documentValue": "JUAN PEREZ GOMEZ", "match": true }
                                              ],
                                              "valid": true
                                            }"""),
                                    @ExampleObject(name = "RUT con datos que no coinciden", value = """
                                            {
                                              "documentType": { "expected": "RUT", "detected": "RUT", "match": true },
                                              "fields": [
                                                { "field": "tipoContribuyente", "formValue": "Persona Juridica", "documentValue": "Persona natural o sucesion iliquida", "match": false }
                                              ],
                                              "valid": false
                                            }"""),
                                    @ExampleObject(name = "El archivo no es del tipo esperado", value = """
                                            {
                                              "documentType": { "expected": "RUT", "detected": "CEDULA", "match": false },
                                              "fields": [],
                                              "valid": false
                                            }""")
                            })),
            @ApiResponse(
                    responseCode = "400",
                    description = "Petición mal formada: falta el archivo, o `expectedDocumentType`/`applicantType` no son un valor válido.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    { "statusCode": 400, "status": "BAD_REQUEST", "message": "expectedDocumentType debe ser CEDULA o RUT", "timestamp": "2026-10-26T20:19:28.305Z" }"""),
                            schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "No se pudo completar la validación por una razón de negocio (ej. el clasificador no reconoció ningún tipo de documento).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error inesperado (ej. Azure Document Intelligence no respondió o devolvió un error).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<DocumentValidationResponseDto> validate(
            @Parameter(description = "Archivo PDF del documento a validar.", required = true)
            @RequestPart("file") FilePart file,

            @Parameter(description = "Tipo de documento que se espera recibir.", required = true,
                    schema = @Schema(allowableValues = {"CEDULA", "RUT"}, example = "CEDULA"))
            @RequestPart("expectedDocumentType") String expectedDocumentTypeRaw,

            @Parameter(description = "Número de documento escrito en el formulario. Solo se usa (y se compara) cuando expectedDocumentType=CEDULA.",
                    schema = @Schema(example = "1020304050"))
            @RequestPart(value = "numeroDocumento", required = false) String numeroDocumento,

            @Parameter(description = "Nombre completo escrito en el formulario. Solo se usa (y se compara) cuando expectedDocumentType=CEDULA.",
                    schema = @Schema(example = "Juan Perez Gomez"))
            @RequestPart(value = "nombreCompleto", required = false) String nombreCompleto,

            @Parameter(description = "Tipo de solicitante. Solo se usa cuando expectedDocumentType=RUT, para elegir qué tipo de contribuyente se espera en el documento.",
                    schema = @Schema(allowableValues = {"NATURAL", "JURIDICA"}, example = "NATURAL"))
            @RequestPart(value = "applicantType", required = false) String applicantTypeRaw);
}
