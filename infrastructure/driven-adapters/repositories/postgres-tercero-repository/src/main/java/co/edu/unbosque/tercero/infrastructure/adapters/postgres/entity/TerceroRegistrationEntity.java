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

/**
 * Implements {@link Persistable} because the id is a client-generated UUID, not a
 * database-generated one: without this, Spring Data R2DBC sees a non-null id and assumes the row
 * already exists, issuing an UPDATE (affecting zero rows, no error) instead of an INSERT.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tercero_registrations")
public class TerceroRegistrationEntity implements Persistable<String> {

    @Id
    private String id;
    private String calidad;
    private String nacionalidad;
    @Column("tipo_persona")
    private String tipoPersona;
    private String correo;
    private String nombre;
    private String cedula;
    @Column("fecha_expedicion_cedula")
    private String fechaExpedicionCedula;
    @Column("lugar_nacimiento")
    private String lugarNacimiento;
    @Column("pais_nacimiento")
    private String paisNacimiento;
    private String nit;
    private String celular;
    private String direccion;
    private String estado;
    @Column("fecha_registro")
    private Instant fechaRegistro;

    @Transient
    @Builder.Default
    private boolean newEntity = true;

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
