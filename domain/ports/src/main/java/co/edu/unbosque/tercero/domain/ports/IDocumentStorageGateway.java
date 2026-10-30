package co.edu.unbosque.tercero.domain.ports;

import reactor.core.publisher.Mono;

/**
 * Outbound port for durably storing an uploaded document (e.g. in Azure Blob Storage).
 */
public interface IDocumentStorageGateway {

    /**
     * Uploads a document and returns the URL it can be retrieved from.
     */
    Mono<String> upload(byte[] content, String fileName, String contentType);
}
