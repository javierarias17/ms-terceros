package co.edu.unbosque.tercero.infrastructure.adapters.blobstorage;

import co.edu.unbosque.tercero.domain.ports.IDocumentStorageGateway;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Uploads a document to Azure Blob Storage and returns its URL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AzureBlobStorageAdapter implements IDocumentStorageGateway {

    private final BlobContainerAsyncClient blobContainerAsyncClient;

    @Override
    public Mono<String> upload(byte[] content, String fileName, String contentType) {
        String blobName = "%s-%s".formatted(UUID.randomUUID(), fileName);
        BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

        log.info("Subiendo documento '{}' ({} bytes, {}) como blob '{}'", fileName, content.length, contentType, blobName);

        return blobAsyncClient.upload(BinaryData.fromBytes(content))
                .then(blobAsyncClient.setHttpHeaders(headers))
                .thenReturn(blobAsyncClient.getBlobUrl())
                .doOnSuccess(url -> log.info("Documento '{}' subido correctamente: {}", fileName, url))
                .doOnError(ex -> log.error("Fallo al subir el documento '{}' a Blob Storage: {}", fileName, ex.getMessage(), ex));
    }

    @Override
    public Mono<String> generateDownloadUrl(String blobUrl) {
        return Mono.fromCallable(() -> {
            String blobName = BlobUrlParts.parse(blobUrl).getBlobName();
            BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);

            OffsetDateTime expiry = OffsetDateTime.now().plusMinutes(15);
            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiry, permission);

            return blobUrl + "?" + blobAsyncClient.generateSas(sasValues);
        }).doOnError(ex -> log.error("Fallo al generar la URL firmada para '{}': {}", blobUrl, ex.getMessage(), ex));
    }
}
