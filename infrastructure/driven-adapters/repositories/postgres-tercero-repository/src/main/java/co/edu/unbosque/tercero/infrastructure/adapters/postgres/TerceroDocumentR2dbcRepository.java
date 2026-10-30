package co.edu.unbosque.tercero.infrastructure.adapters.postgres;

import co.edu.unbosque.tercero.infrastructure.adapters.postgres.entity.TerceroDocumentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TerceroDocumentR2dbcRepository extends ReactiveCrudRepository<TerceroDocumentEntity, String> {
}
