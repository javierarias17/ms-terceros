package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.DocumentType;
import reactor.core.publisher.Mono;

/**
 * Outbound port for classifying an uploaded document (e.g. via an Azure Document Intelligence
 * custom classifier). Implementations return {@link DocumentType#UNKNOWN} when the document does
 * not match any of the known types, rather than failing.
 */
public interface IDocumentClassifierGateway {

    Mono<DocumentType> classify(byte[] document);
}
