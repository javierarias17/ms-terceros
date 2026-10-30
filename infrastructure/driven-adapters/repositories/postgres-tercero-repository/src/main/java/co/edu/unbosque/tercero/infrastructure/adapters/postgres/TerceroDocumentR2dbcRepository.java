package co.edu.unbosque.tercero.infrastructure.adapters.postgres;

import co.edu.unbosque.tercero.infrastructure.adapters.postgres.entity.TerceroDocumentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TerceroDocumentR2dbcRepository extends ReactiveCrudRepository<TerceroDocumentEntity, String> {

    Flux<TerceroDocumentEntity> findByTerceroRegistrationId(String terceroRegistrationId);
}
