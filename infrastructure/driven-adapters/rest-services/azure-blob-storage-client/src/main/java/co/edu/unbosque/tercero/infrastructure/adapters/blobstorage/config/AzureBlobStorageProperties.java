package co.edu.unbosque.tercero.infrastructure.adapters.blobstorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the Azure Storage account (connection string, container) from {@code blob-storage.*} in
 * application.yml. La connection string se saca del Portal del Storage Account > "Access keys".
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "blob-storage")
public class AzureBlobStorageProperties {

    private String connectionString;
    private String containerName = "tercero-documents";
}
