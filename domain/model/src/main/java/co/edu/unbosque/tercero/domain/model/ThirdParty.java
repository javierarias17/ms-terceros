package co.edu.unbosque.tercero.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Sample domain entity. Replace with the real domain models of this service.
 */
@Getter
@Builder
@AllArgsConstructor
public class ThirdParty {

    private final String id;
    private final String name;
    private final String documentType;
    private final String documentNumber;
}
