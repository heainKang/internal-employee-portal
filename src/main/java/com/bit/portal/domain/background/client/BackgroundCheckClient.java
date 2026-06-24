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
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
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
                .onStatus(HttpStatusCode::is4xxClientError, resp -> handle4xx(resp, "create"))
                .onStatus(HttpStatusCode::is5xxServerError, resp -> handle5xx(resp, "create"))
                .bodyToMono(BackgroundCheckCreatedResponse.class)
                .onErrorMap(TimeoutException.class, e -> {
                    log.error("[create] Response timeout");
                    return new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT);
                })
                .onErrorMap(WebClientRequestException.class, e -> {
                    log.error("[create] Connection error: {}", e.getMessage());
                    return new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
                })
                .block();
    }

    /** GET /background-checks/{checkId} — 단건 결과 조회 */
    public BackgroundCheckResultResponse getCheck(String checkId) {
        return webClient.get()
                .uri("/background-checks/{checkId}", checkId)
                .retrieve()
                .onStatus(status -> status.value() == 404, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .thenReturn(new BusinessException(ErrorCode.BACKGROUND_CHECK_NOT_FOUND)))
                .onStatus(HttpStatusCode::is4xxClientError, resp -> handle4xx(resp, "getCheck"))
                .onStatus(HttpStatusCode::is5xxServerError, resp -> handle5xx(resp, "getCheck"))
                .bodyToMono(BackgroundCheckResultResponse.class)
                .onErrorMap(TimeoutException.class, e -> {
                    log.error("[getCheck] Response timeout");
                    return new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT);
                })
                .onErrorMap(WebClientRequestException.class, e -> {
                    log.error("[getCheck] Connection error: {}", e.getMessage());
                    return new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
                })
                .block();
    }

    /** GET /background-checks?employeeId={employeeId} — 직원별 이력 조회 */
    public BackgroundCheckListResponse listChecks(String employeeId) {
        return webClient.get()
                .uri(uri -> uri.path("/background-checks")
                               .queryParam("employeeId", employeeId)
                               .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> handle4xx(resp, "listChecks"))
                .onStatus(HttpStatusCode::is5xxServerError, resp -> handle5xx(resp, "listChecks"))
                .bodyToMono(BackgroundCheckListResponse.class)
                .onErrorMap(TimeoutException.class, e -> {
                    log.error("[listChecks] Response timeout");
                    return new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT);
                })
                .onErrorMap(WebClientRequestException.class, e -> {
                    log.error("[listChecks] Connection error: {}", e.getMessage());
                    return new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
                })
                .block();
    }

    // ── 공통 에러 핸들러 ────────────────────────────────────────────────────────

    /**
     * 4xx — 클라이언트 요청 오류 → BusinessException (CB 카운트 제외)
     */
    private Mono<? extends Throwable> handle4xx(ClientResponse resp, String ctx) {
        int status = resp.statusCode().value();
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .doOnNext(body -> log.warn("[{}] {} Client Error: {}", ctx, status, body))
                .thenReturn(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));
    }

    /**
     * 5xx — 서버 오류 → ExternalApiException (CB 카운트)
     *   503 → EXTERNAL_API_UNAVAILABLE (X001, HTTP 503)  외부 서비스 일시 불가
     *   500 / 기타 5xx → EXTERNAL_API_ERROR (X003, HTTP 502)  외부 서버 내부 오류
     */
    private Mono<? extends Throwable> handle5xx(ClientResponse resp, String ctx) {
        int status = resp.statusCode().value();
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .doOnNext(body -> log.error("[{}] {} Server Error: {}", ctx, status, body))
                .thenReturn(status == 503
                        ? new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE)
                        : new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR));
    }
}
