package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.ThirdParty;
import reactor.core.publisher.Mono;

/**
 * Outbound port for retrieving third-party data from an external system.
 * Sample port kept as a reference for the hexagonal pattern used by this service:
 * implementations live in infrastructure/driven-adapters.
 */
public interface IThirdPartyGateway {

    Mono<ThirdParty> findById(String id);
}
