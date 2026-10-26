package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence;

import co.edu.unbosque.tercero.domain.model.exception.BusinessException;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.config.AzureDocumentIntelligenceProperties;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto.AnalyzeOperationDto;
import co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto.AnalyzeResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

/**
 * Submits a document to one of the Azure Document Intelligence "analyze" endpoints (a custom
 * classifier or a custom extraction model) and polls the returned {@code Operation-Location}
 * until the asynchronous analysis finishes, as the Document Intelligence REST API requires.
 * <p>
 * Both the initial submit and the polling calls retry on HTTP 429 (the F0/free tier has a low
 * requests-per-minute limit) instead of failing the whole validation on a transient rate limit.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AzureAnalyzeOperationPoller {

    private static final String OPERATION_LOCATION_HEADER = "Operation-Location";
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String STATUS_SUCCEEDED = "succeeded";
    private static final String STATUS_FAILED = "failed";

    private final WebClient azureDocumentIntelligenceWebClient;
    private final AzureDocumentIntelligenceProperties properties;

    public Mono<AnalyzeResultDto> analyze(String analyzePath, byte[] document) {
        return submit(analyzePath, document, 1)
                .flatMap(operationLocation -> pollOnce(operationLocation, 1));
    }

    private Mono<String> submit(String analyzePath, byte[] document, int attempt) {
        return azureDocumentIntelligenceWebClient.post()
                .uri(analyzePath)
                .contentType(MediaType.APPLICATION_PDF)
                .bodyValue(document)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getHeaders().getFirst(OPERATION_LOCATION_HEADER))
                .switchIfEmpty(Mono.error(new BusinessException(
                        "DOC_ANALYZE_NO_OPERATION", "Azure Document Intelligence did not return an Operation-Location header")))
                .onErrorResume(WebClientResponseException.TooManyRequests.class,
                        ex -> retryAfterRateLimit(attempt, ex, nextAttempt -> submit(analyzePath, document, nextAttempt)));
    }

    private Mono<AnalyzeResultDto> pollOnce(String operationLocationUrl, int attempt) {
        return azureDocumentIntelligenceWebClient.get()
                .uri(operationLocationUrl)
                .retrieve()
                .bodyToMono(AnalyzeOperationDto.class)
                .flatMap(operation -> resolve(operationLocationUrl, attempt, operation))
                .onErrorResume(WebClientResponseException.TooManyRequests.class,
                        ex -> retryAfterRateLimit(attempt, ex, nextAttempt -> pollOnce(operationLocationUrl, nextAttempt)));
    }

    private Mono<AnalyzeResultDto> resolve(String operationLocationUrl, int attempt, AnalyzeOperationDto operation) {
        if (STATUS_SUCCEEDED.equalsIgnoreCase(operation.getStatus())) {
            return Mono.just(operation.getAnalyzeResult());
        }
        if (STATUS_FAILED.equalsIgnoreCase(operation.getStatus())) {
            return Mono.error(new BusinessException("DOC_ANALYZE_FAILED", "Azure Document Intelligence analysis failed"));
        }
        if (attempt >= properties.getPolling().getMaxAttempts()) {
            return Mono.error(new BusinessException("DOC_ANALYZE_TIMEOUT", "Timed out waiting for Azure Document Intelligence result"));
        }
        return Mono.delay(Duration.ofMillis(properties.getPolling().getIntervalMillis()))
                .then(pollOnce(operationLocationUrl, attempt + 1));
    }

    private <T> Mono<T> retryAfterRateLimit(int attempt, WebClientResponseException ex, java.util.function.Function<Integer, Mono<T>> retry) {
        if (attempt >= properties.getPolling().getMaxAttempts()) {
            return Mono.error(new BusinessException(
                    "DOC_ANALYZE_RATE_LIMITED", "Azure Document Intelligence rate limit (429) exceeded too many times"));
        }
        Duration wait = retryAfter(ex).orElse(Duration.ofMillis(properties.getPolling().getIntervalMillis() * 3L));
        log.warn("Azure Document Intelligence rate limited (429), retrying in {} [attempt {}]", wait, attempt);
        return Mono.delay(wait).then(retry.apply(attempt + 1));
    }

    private Optional<Duration> retryAfter(WebClientResponseException ex) {
        String header = ex.getHeaders().getFirst(RETRY_AFTER_HEADER);
        if (header == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Duration.ofSeconds(Long.parseLong(header.trim())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
