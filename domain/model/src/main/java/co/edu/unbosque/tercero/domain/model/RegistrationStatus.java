package co.edu.unbosque.tercero.domain.model;

/**
 * Lifecycle state of a {@link TerceroRegistration}. Set to {@link #REGISTRADO} when the record
 * is created; nothing in this codebase changes it yet — that belongs to a future approval flow
 * (an "Operativo" reviewing and approving/rejecting the request).
 */
public enum RegistrationStatus {
    REGISTRADO,
    APROBADO,
    RECHAZADO
}
