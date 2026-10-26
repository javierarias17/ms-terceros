package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.DocumentType;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Outbound port for extracting fields from an uploaded document (e.g. via one of the Azure
 * Document Intelligence custom extraction models). The returned map uses the field names as
 * defined in the extraction model (e.g. {@code cedula_numero}, {@code tipo_contribuyente}).
 */
public interface IDocumentExtractorGateway {

    Mono<Map<String, String>> extractFields(DocumentType documentType, byte[] document);
}
