package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.TerceroDocument;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.domain.ports.IDocumentStorageGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Fetches a single tercero registration for the admin panel's detail view, replacing each
 * document's stored (private) URL with a short-lived signed one the browser can actually open.
 */
@RequiredArgsConstructor
public class GetTerceroRegistrationUseCase {

    private final ITerceroRegistrationRepository terceroRegistrationRepository;
    private final IDocumentStorageGateway documentStorageGateway;

    public Mono<TerceroRegistration> execute(String id) {
        return terceroRegistrationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No existe un registro de tercero con id " + id)))
                .flatMap(this::withSignedDocumentUrls);
    }

    private Mono<TerceroRegistration> withSignedDocumentUrls(TerceroRegistration registration) {
        return Flux.fromIterable(registration.getDocuments())
                .flatMap(this::withSignedUrl)
                .collectList()
                .map(documents -> registration.toBuilder().documents(documents).build());
    }

    private Mono<TerceroDocument> withSignedUrl(TerceroDocument document) {
        return documentStorageGateway.generateDownloadUrl(document.getUrl())
                .map(signedUrl -> document.toBuilder().url(signedUrl).build());
    }
}
