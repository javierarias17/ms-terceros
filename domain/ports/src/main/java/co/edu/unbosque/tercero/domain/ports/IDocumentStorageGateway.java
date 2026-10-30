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

    /**
     * Turns a stored (private) blob URL into a short-lived, signed URL a browser can actually
     * open — the container isn't public, so the plain URL saved in {@code tercero_documents}
     * isn't enough on its own.
     */
    Mono<String> generateDownloadUrl(String blobUrl);
}
