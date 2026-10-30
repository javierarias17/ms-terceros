package co.edu.unbosque.tercero.infrastructure.adapters.postgres.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Same {@link Persistable} reasoning as {@link TerceroRegistrationEntity}: client-generated id. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tercero_documents")
public class TerceroDocumentEntity implements Persistable<String> {

    @Id
    private String id;
    @Column("tercero_registration_id")
    private String terceroRegistrationId;
    @Column("document_key")
    private String documentKey;
    private String url;
    @Column("file_name")
    private String fileName;
    @Column("content_type")
    private String contentType;
    @Column("uploaded_at")
    private Instant uploadedAt;

    @Transient
    @Builder.Default
    private boolean newEntity = true;

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
