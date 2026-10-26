package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.ApplicantType;
import co.edu.unbosque.tercero.domain.model.DocumentValidationRequest;
import co.edu.unbosque.tercero.domain.model.DocumentValidationResult;
import co.edu.unbosque.tercero.domain.model.FieldComparison;
import co.edu.unbosque.tercero.domain.ports.IDocumentClassifierGateway;
import co.edu.unbosque.tercero.domain.ports.IDocumentExtractorGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Validates an uploaded document in two steps: (1) confirms the document is really the expected
 * type (cédula / RUT) via the classifier, (2) when it is, extracts its fields and compares them
 * against the values entered in the form.
 */
@RequiredArgsConstructor
public class ValidateDocumentUseCase {

    private static final String FIELD_CEDULA_NUMERO = "cedula_numero";
    private static final String FIELD_CEDULA_NOMBRES = "cedula_nombres";
    private static final String FIELD_CEDULA_APELLIDOS = "cedula_apellidos";
    private static final String FIELD_TIPO_CONTRIBUYENTE = "tipo_contribuyente";

    private final IDocumentClassifierGateway classifierGateway;
    private final IDocumentExtractorGateway extractorGateway;
    private final String tipoContribuyenteNatural;
    private final String tipoContribuyenteJuridica;

    public Mono<DocumentValidationResult> execute(DocumentValidationRequest request) {
        return classifierGateway.classify(request.getDocumentContent())
                .flatMap(detectedType -> {
                    boolean typeMatch = detectedType == request.getExpectedType();
                    if (!typeMatch) {
                        return Mono.just(DocumentValidationResult.builder()
                                .expectedType(request.getExpectedType())
                                .detectedType(detectedType)
                                .documentTypeMatch(false)
                                .build());
                    }
                    return extractorGateway.extractFields(detectedType, request.getDocumentContent())
                            .map(fields -> DocumentValidationResult.builder()
                                    .expectedType(request.getExpectedType())
                                    .detectedType(detectedType)
                                    .documentTypeMatch(true)
                                    .fieldComparisons(buildFieldComparisons(request, fields))
                                    .build());
                });
    }

    private List<FieldComparison> buildFieldComparisons(DocumentValidationRequest request, Map<String, String> fields) {
        return switch (request.getExpectedType()) {
            case CEDULA -> buildCedulaComparisons(request, fields);
            case RUT -> buildRutComparisons(request, fields);
            case UNKNOWN -> List.of();
        };
    }

    private List<FieldComparison> buildCedulaComparisons(DocumentValidationRequest request, Map<String, String> fields) {
        String numeroDocumento = removeDots(fields.get(FIELD_CEDULA_NUMERO));
        String nombresApellidos = joinNonBlank(fields.get(FIELD_CEDULA_NOMBRES), fields.get(FIELD_CEDULA_APELLIDOS));

        FieldComparison numero = new FieldComparison(
                "numeroDocumento", request.getNumeroDocumento(), numeroDocumento,
                exactMatch(request.getNumeroDocumento(), numeroDocumento));

        FieldComparison nombre = new FieldComparison(
                "nombreCompleto", request.getNombreCompleto(), nombresApellidos,
                namesMatch(request.getNombreCompleto(), nombresApellidos));

        return List.of(numero, nombre);
    }

    private List<FieldComparison> buildRutComparisons(DocumentValidationRequest request, Map<String, String> fields) {
        String tipoContribuyente = fields.get(FIELD_TIPO_CONTRIBUYENTE);
        String expected = request.getApplicantType() == ApplicantType.JURIDICA
                ? tipoContribuyenteJuridica
                : tipoContribuyenteNatural;

        FieldComparison comparison = new FieldComparison(
                "tipoContribuyente", expected, tipoContribuyente,
                normalizedEquals(expected, tipoContribuyente));

        return List.of(comparison);
    }

    private boolean exactMatch(String formValue, String documentValue) {
        return formValue != null && documentValue != null
                && formValue.trim().equals(documentValue.trim());
    }

    private boolean normalizedEquals(String a, String b) {
        return !normalize(a).isEmpty() && normalize(a).equals(normalize(b));
    }

    private boolean namesMatch(String formValue, String documentValue) {
        Set<String> formTokens = tokenize(formValue);
        Set<String> documentTokens = tokenize(documentValue);
        return !formTokens.isEmpty() && formTokens.equals(documentTokens);
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(normalize(value).split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String stripped = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return stripped.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    private String removeDots(String value) {
        return value == null ? null : value.replace(".", "");
    }

    private String joinNonBlank(String... parts) {
        return Arrays.stream(parts)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));
    }
}
