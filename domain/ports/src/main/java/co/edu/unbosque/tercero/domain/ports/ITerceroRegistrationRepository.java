package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for persisting and querying {@link TerceroRegistration} (e.g. in PostgreSQL via
 * R2DBC).
 */
public interface ITerceroRegistrationRepository {

    Mono<TerceroRegistration> save(TerceroRegistration registration);

    /**
     * Lists registrations, optionally filtered by {@code estado}. A {@code null} estado returns
     * every registration.
     */
    Flux<TerceroRegistration> findAll(RegistrationStatus estado);

    Mono<TerceroRegistration> findById(String id);

    Mono<TerceroRegistration> updateEstado(String id, RegistrationStatus estado, String motivoRechazo);
}
