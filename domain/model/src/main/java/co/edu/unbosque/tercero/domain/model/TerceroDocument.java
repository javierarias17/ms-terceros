package co.edu.unbosque.tercero.domain.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/**
 * A document already uploaded to storage and linked to a {@link TerceroRegistration} — one row
 * per document, not a map embedded in the registration, so it can be queried/filtered on its own
 * (e.g. "every registration missing a RUT").
 */
@Getter
@Builder
public class TerceroDocument {

    private final String id;
    private final String terceroRegistrationId;
    private final String documentKey;
    private final String url;
    private final String fileName;
    private final String contentType;
    private final Instant uploadedAt;
}
