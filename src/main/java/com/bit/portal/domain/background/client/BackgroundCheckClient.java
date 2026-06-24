package com.bit.portal.domain.background.client;

import com.bit.portal.domain.background.dto.*;
import com.bit.portal.global.error.code.ErrorCode;
import com.bit.portal.global.error.exception.BusinessException;
import com.bit.portal.global.error.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundCheckClient {

    @Qualifier("backgroundCheckWebClient")
    private final WebClient webClient;

    /** POST /background-checks — 배경 조회 요청 */
    public BackgroundCheckCreatedResponse createCheck(BackgroundCheckApiRequest request) {
        return webClient.post()
                .uri("/background-checks")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class)
                                .doOnNext(body -> log.warn("Background check 4xx: {}", body))
                                .flatMap(body -> Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR))))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR)))
                .bodyToMono(BackgroundCheckCreatedResponse.class)
                .onErrorMap(TimeoutException.class, e -> new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT))
                .block();
    }

    /** GET /background-checks/{checkId} — 단건 결과 조회 */
    public BackgroundCheckResultResponse getCheck(String checkId) {
        return webClient.get()
                .uri("/background-checks/{checkId}", checkId)
                .retrieve()
                .onStatus(status -> status.value() == 404, resp ->
                        Mono.error(new BusinessException(ErrorCode.BACKGROUND_CHECK_NOT_FOUND)))
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR)))
                .bodyToMono(BackgroundCheckResultResponse.class)
                .onErrorMap(TimeoutException.class, e -> new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT))
                .block();
    }

    /** GET /background-checks?employeeId={employeeId} — 직원별 이력 조회 */
    public BackgroundCheckListResponse listChecks(String employeeId) {
        return webClient.get()
                .uri(uri -> uri
                        .path("/background-checks")
                        .queryParam("employeeId", employeeId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR)))
                .bodyToMono(BackgroundCheckListResponse.class)
                .onErrorMap(TimeoutException.class, e -> new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT))
                .block();
    }
}
