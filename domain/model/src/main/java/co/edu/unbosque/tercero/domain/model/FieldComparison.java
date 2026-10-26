package co.edu.unbosque.tercero.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of comparing one field entered in the form against the same field extracted
 * from the uploaded document.
 */
@Getter
@AllArgsConstructor
public class FieldComparison {

    private final String fieldName;
    private final String formValue;
    private final String documentValue;
    private final boolean match;
}
