package co.edu.unbosque.tercero.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

/**
 * Outcome of validating an uploaded document: whether it is the expected document type,
 * and (when it is) whether each relevant field matches the value entered in the form.
 */
@Getter
@Builder
public class DocumentValidationResult {

    private final DocumentType expectedType;
    private final DocumentType detectedType;
    private final boolean documentTypeMatch;
    @Singular("fieldComparison")
    private final List<FieldComparison> fieldComparisons;

    public boolean isValid() {
        return documentTypeMatch && fieldComparisons.stream().allMatch(FieldComparison::isMatch);
    }
}
