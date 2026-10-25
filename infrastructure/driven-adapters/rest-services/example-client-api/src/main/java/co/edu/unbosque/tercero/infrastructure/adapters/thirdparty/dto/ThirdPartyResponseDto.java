package co.edu.unbosque.tercero.infrastructure.adapters.thirdparty.dto;

import lombok.Data;

/**
 * Shape returned by the external service. Kept separate from the domain model
 * so the adapter can absorb upstream contract changes without touching the domain.
 */
@Data
public class ThirdPartyResponseDto {

    private String id;
    private String name;
    private String documentType;
    private String documentNumber;
}
