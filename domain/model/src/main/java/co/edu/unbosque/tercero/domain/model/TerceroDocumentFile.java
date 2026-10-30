package co.edu.unbosque.tercero.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * One uploaded file as part of a {@link TerceroRegistrationRequest} — the frontend's document key
 * (e.g. {@code prov_cedula}, {@code cli_rut}, {@code prov_certBancaria}), not just cédula/RUT.
 */
@Getter
@Builder
public class TerceroDocumentFile {

    private final String key;
    private final String fileName;
    private final String contentType;
    private final byte[] content;
}
