package co.edu.unbosque.tercero.domain.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * A completed tercero registration: every field the applicant filled in the form, plus where
 * their documents ended up (Blob Storage URLs) and the record's lifecycle state.
 */
@Getter
@Builder
public class TerceroRegistration {

    private final String id;
    private final String calidad;
    private final String nacionalidad;
    private final String tipoPersona;
    private final String correo;
    private final String nombre;
    private final String cedula;
    private final String fechaExpedicionCedula;
    private final String lugarNacimiento;
    private final String paisNacimiento;
    private final String nit;
    private final String celular;
    private final String direccion;
    private final List<TerceroDocument> documents;
    private final RegistrationStatus estado;
    private final Instant fechaRegistro;
}
