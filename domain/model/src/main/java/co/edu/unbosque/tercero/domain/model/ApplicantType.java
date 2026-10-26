package co.edu.unbosque.tercero.domain.model;

/**
 * Type of applicant filling the form ("Persona" selector), used to pick the expected
 * {@code tipo_contribuyente} value when validating a RUT.
 */
public enum ApplicantType {
    NATURAL,
    JURIDICA
}
