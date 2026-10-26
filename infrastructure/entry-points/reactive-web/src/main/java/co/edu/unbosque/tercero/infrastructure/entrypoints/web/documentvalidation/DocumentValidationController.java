package co.edu.unbosque.tercero.infrastructure.entrypoints.web.documentvalidation;

import co.edu.unbosque.tercero.domain.model.ApplicantType;
import co.edu.unbosque.tercero.domain.model.DocumentType;
import co.edu.unbosque.tercero.domain.model.DocumentValidationRequest;
import co.edu.unbosque.tercero.domain.model.DocumentValidationResult;
import co.edu.unbosque.tercero.domain.usecases.ValidateDocumentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Validates a cédula or RUT uploaded by the applicant: confirms it is really the expected
 * document type and, if so, that its fields match the values entered in the form.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/document-validations")
public class DocumentValidationController {

    private final ValidateDocumentUseCase validateDocumentUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<DocumentValidationResponseDto> validate(
            @RequestPart("file") FilePart file,
            @RequestPart("expectedDocumentType") String expectedDocumentTypeRaw,
            @RequestPart(value = "numeroDocumento", required = false) String numeroDocumento,
            @RequestPart(value = "nombreCompleto", required = false) String nombreCompleto,
            @RequestPart(value = "applicantType", required = false) String applicantTypeRaw) {

        DocumentType expectedDocumentType = parseDocumentType(expectedDocumentTypeRaw);
        ApplicantType applicantType = parseApplicantType(applicantTypeRaw);

        return readContent(file)
                .map(content -> DocumentValidationRequest.builder()
                        .expectedType(expectedDocumentType)
                        .documentContent(content)
                        .numeroDocumento(numeroDocumento)
                        .nombreCompleto(nombreCompleto)
                        .applicantType(applicantType)
                        .build())
                .flatMap(validateDocumentUseCase::execute)
                .map(this::toResponseDto);
    }

    private Mono<byte[]> readContent(FilePart file) {
        return DataBufferUtils.join(file.content())
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return bytes;
                });
    }

    private DocumentType parseDocumentType(String raw) {
        DocumentType type = parseEnum(DocumentType.class, raw, "expectedDocumentType debe ser CEDULA o RUT");
        if (type == DocumentType.UNKNOWN) {
            throw new IllegalArgumentException("expectedDocumentType debe ser CEDULA o RUT");
        }
        return type;
    }

    private ApplicantType parseApplicantType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return parseEnum(ApplicantType.class, raw, "applicantType debe ser NATURAL o JURIDICA");
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String raw, String errorMessage) {
        try {
            return Enum.valueOf(enumType, raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private DocumentValidationResponseDto toResponseDto(DocumentValidationResult result) {
        DocumentTypeResultDto documentType = new DocumentTypeResultDto(
                result.getExpectedType().name(),
                result.getDetectedType() == null ? null : result.getDetectedType().name(),
                result.isDocumentTypeMatch());

        var fields = result.getFieldComparisons().stream()
                .map(fc -> new FieldComparisonDto(fc.getFieldName(), fc.getFormValue(), fc.getDocumentValue(), fc.isMatch()))
                .toList();

        return new DocumentValidationResponseDto(documentType, fields, result.isValid());
    }
}
