package co.edu.unbosque.tercero.infrastructure.entrypoints.web;

import co.edu.unbosque.tercero.domain.model.ThirdParty;
import co.edu.unbosque.tercero.domain.usecases.GetThirdPartyUseCase;
import co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared.ApiSuccessResponse;
import co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Sample entry point kept as a reference for how controllers call into use cases
 * and wrap the result with {@link ResponseFactory}. Replace with the real endpoints
 * of this service and delete this sample once done.
 */
@RestController
@RequiredArgsConstructor
public class ThirdPartyController {

    private final GetThirdPartyUseCase getThirdPartyUseCase;

    @GetMapping("/v1/third-parties/{id}")
    public Mono<ApiSuccessResponse<ThirdParty>> getById(
            @PathVariable String id,
            @RequestHeader(value = "Transaction-Id", required = false) String transactionId) {
        return getThirdPartyUseCase.execute(id)
                .map(thirdParty -> ResponseFactory.buildSuccessResponse(HttpStatus.OK, thirdParty, transactionId));
    }
}
