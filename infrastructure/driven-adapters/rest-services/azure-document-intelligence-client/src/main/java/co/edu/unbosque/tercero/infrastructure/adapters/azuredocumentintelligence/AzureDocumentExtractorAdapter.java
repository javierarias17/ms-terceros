package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence;

import co.edu.unbosque.tercero.domain.model.DocumentType;
import co.edu.unbosque.tercero.domain.model.exception.BusinessException;
import co.edu.unbosque.tercero.domain.ports.IDocumentExtractorGateway;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.config.AzureDocumentIntelligenceProperties;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto.AnalyzeResultDto;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto.DocumentFieldDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calls the custom Azure Document Intelligence extraction model that matches the already
 * classified document type (one model for cédula, another for RUT) and flattens the result into
 * a {@code fieldName -> recognized text} map.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AzureDocumentExtractorAdapter implements IDocumentExtractorGateway {

    private final AzureAnalyzeOperationPoller poller;
    private final AzureDocumentIntelligenceProperties properties;

    @Override
    public Mono<Map<String, String>> extractFields(DocumentType documentType, byte[] document) {
        String modelId = resolveModelId(documentType);
        String path = "/documentintelligence/documentModels/%s:analyze?api-version=%s"
                .formatted(modelId, properties.getApiVersion());

        return poller.analyze(path, document).map(this::toFieldMap);
    }

    private String resolveModelId(DocumentType documentType) {
        return switch (documentType) {
            case CEDULA -> properties.getModels().getCedulaModelId();
            case RUT -> properties.getModels().getRutModelId();
            case UNKNOWN -> throw new BusinessException(
                    "DOC_TYPE_UNKNOWN", "Cannot extract fields: unknown document type");
        };
    }

    private Map<String, String> toFieldMap(AnalyzeResultDto result) {
        if (result == null || result.getDocuments() == null || result.getDocuments().isEmpty()) {
            return Map.of();
        }
        Map<String, DocumentFieldDto> fields = result.getDocuments().get(0).getFields();
        if (fields == null) {
            return Map.of();
        }
        log.warn("Extractor raw fields: {}", fields.entrySet().stream()
                .map(e -> "%s=[content=%s, confidence=%s]".formatted(
                        e.getKey(),
                        e.getValue() == null ? null : e.getValue().getContent(),
                        e.getValue() == null ? null : e.getValue().getConfidence()))
                .collect(Collectors.joining(", ")));
        return fields.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().getContent() != null)
                .filter(this::meetsConfidenceThreshold)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getContent()));
    }

    private boolean meetsConfidenceThreshold(Map.Entry<String, DocumentFieldDto> entry) {
        Double confidence = entry.getValue().getConfidence();
        boolean meets = confidence != null && confidence >= properties.getMinConfidence();
        if (!meets) {
            log.warn("Extracted field '{}' confidence {} below threshold {}, discarding",
                    entry.getKey(), confidence, properties.getMinConfidence());
        }
        return meets;
    }
}
