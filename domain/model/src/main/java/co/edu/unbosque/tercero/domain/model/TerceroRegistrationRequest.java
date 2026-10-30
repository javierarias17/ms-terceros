package co.edu.unbosque.tercero.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Input for {@link co.edu.unbosque.tercero.domain.usecases.FinalizeTerceroRegistrationUseCase}:
 * every field from the registration form plus whatever documents the applicant uploaded.
 */
@Getter
@Builder
public class TerceroRegistrationRequest {

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
    private final List<TerceroDocumentFile> documents;
}
