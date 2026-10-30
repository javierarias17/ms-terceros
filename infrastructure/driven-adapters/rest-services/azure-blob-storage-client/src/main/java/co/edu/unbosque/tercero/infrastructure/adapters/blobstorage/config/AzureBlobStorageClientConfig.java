package co.edu.unbosque.tercero.infrastructure.adapters.blobstorage.config;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AzureBlobStorageClientConfig {

    private final AzureBlobStorageProperties properties;

    @Bean
    public BlobContainerAsyncClient blobContainerAsyncClient() {
        BlobServiceAsyncClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(properties.getConnectionString())
                .buildAsyncClient();
        BlobContainerAsyncClient containerClient =
                serviceClient.getBlobContainerAsyncClient(properties.getContainerName());
        // Solo pasa una vez, al levantar la app (no en el camino reactivo de una petición), así
        // que bloquear aquí es aceptable: nos asegura que el contenedor exista antes de aceptar
        // tráfico, en vez de descubrir el 404 en el primer upload real.
        containerClient.createIfNotExists().block();
        return containerClient;
    }
}
