package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.ThirdParty;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.domain.ports.IThirdPartyGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Sample use case kept as a reference for how use cases are wired in this service:
 * plain classes with no {@code @Service}, receiving their output ports by constructor
 * and registered as a {@code @Bean} in the application module.
 */
@RequiredArgsConstructor
public class GetThirdPartyUseCase {

    private final IThirdPartyGateway thirdPartyGateway;

    public Mono<ThirdParty> execute(String id) {
        return thirdPartyGateway.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Third party not found: " + id)));
    }
}
