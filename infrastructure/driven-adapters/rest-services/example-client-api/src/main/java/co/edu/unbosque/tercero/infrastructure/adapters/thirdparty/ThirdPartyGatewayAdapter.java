package co.edu.unbosque.tercero.infrastructure.adapters.thirdparty;

import co.edu.unbosque.tercero.domain.model.ThirdParty;
import co.edu.unbosque.tercero.domain.ports.IThirdPartyGateway;
import co.edu.unbosque.tercero.infrastructure.adapters.thirdparty.dto.ThirdPartyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Sample driven-adapter: calls an external REST service and maps its response
 * to the domain model. Replace with the real integration for this service
 * (e.g. an Azure Document Intelligence client) and delete this sample once done.
 */
@Component
@RequiredArgsConstructor
public class ThirdPartyGatewayAdapter implements IThirdPartyGateway {

    private final WebClient thirdPartyWebClient;

    @Override
    public Mono<ThirdParty> findById(String id) {
        return thirdPartyWebClient.get()
                .uri("/third-parties/{id}", id)
                .retrieve()
                .bodyToMono(ThirdPartyResponseDto.class)
                .map(this::toDomain);
    }

    private ThirdParty toDomain(ThirdPartyResponseDto dto) {
        return ThirdParty.builder()
                .id(dto.getId())
                .name(dto.getName())
                .documentType(dto.getDocumentType())
                .documentNumber(dto.getDocumentNumber())
                .build();
    }
}
