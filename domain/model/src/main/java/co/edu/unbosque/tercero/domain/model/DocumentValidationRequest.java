package co.edu.unbosque.tercero.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Input for {@link co.edu.unbosque.tercero.domain.usecases.ValidateDocumentUseCase}: the uploaded
 * document plus the subset of form values relevant to the expected document type.
 * <p>
 * Only the fields relevant to {@code expectedType} need to be populated: {@code numeroDocumento}
 * and {@code nombreCompleto} for {@link DocumentType#CEDULA}, {@code applicantType} for
 * {@link DocumentType#RUT}.
 * </p>
 */
@Getter
@Builder
public class DocumentValidationRequest {

    private final DocumentType expectedType;
    private final byte[] documentContent;
    private final String numeroDocumento;
    private final String nombreCompleto;
    private final ApplicantType applicantType;
}
