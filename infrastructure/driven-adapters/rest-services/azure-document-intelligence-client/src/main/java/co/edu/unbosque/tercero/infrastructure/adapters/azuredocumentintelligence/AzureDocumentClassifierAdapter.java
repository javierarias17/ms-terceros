package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence;

import co.edu.unbosque.tercero.domain.model.DocumentType;
import co.edu.unbosque.tercero.domain.ports.IDocumentClassifierGateway;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.config.AzureDocumentIntelligenceProperties;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto.AnalyzeDocumentDto;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto.AnalyzeResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Calls the custom Azure Document Intelligence classifier model to tell cédula apart from RUT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AzureDocumentClassifierAdapter implements IDocumentClassifierGateway {

    private final AzureAnalyzeOperationPoller poller;
    private final AzureDocumentIntelligenceProperties properties;

    @Override
    public Mono<DocumentType> classify(byte[] document) {
        String path = "/documentintelligence/documentClassifiers/%s:analyze?api-version=%s"
                .formatted(properties.getClassifier().getModelId(), properties.getApiVersion());

        return poller.analyze(path, document)
                .map(this::resolveDocumentType)
                .defaultIfEmpty(DocumentType.UNKNOWN);
    }

    private DocumentType resolveDocumentType(AnalyzeResultDto result) {
        if (result == null || result.getDocuments() == null || result.getDocuments().isEmpty()) {
            return DocumentType.UNKNOWN;
        }
        AnalyzeDocumentDto document = result.getDocuments().get(0);
        String docType = document.getDocType();
        if (docType == null) {
            return DocumentType.UNKNOWN;
        }
        Double confidence = document.getConfidence();
        if (confidence == null || confidence < properties.getMinConfidence()) {
            log.warn("Classifier confidence {} below threshold {} for docType '{}', treating as UNKNOWN",
                    confidence, properties.getMinConfidence(), docType);
            return DocumentType.UNKNOWN;
        }

        AzureDocumentIntelligenceProperties.Classifier classifier = properties.getClassifier();
        if (docType.equalsIgnoreCase(classifier.getLabelCedula())) {
            return DocumentType.CEDULA;
        }
        if (docType.equalsIgnoreCase(classifier.getLabelRut())) {
            return DocumentType.RUT;
        }
        return DocumentType.UNKNOWN;
    }
}
