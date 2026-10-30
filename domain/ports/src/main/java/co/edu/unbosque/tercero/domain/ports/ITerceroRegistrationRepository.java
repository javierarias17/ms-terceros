package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import reactor.core.publisher.Mono;

/**
 * Outbound port for persisting a {@link TerceroRegistration} (e.g. in PostgreSQL via R2DBC).
 */
public interface ITerceroRegistrationRepository {

    Mono<TerceroRegistration> save(TerceroRegistration registration);
}
