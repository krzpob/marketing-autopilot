package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramErrorResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
class InstagramGraphClient {

    private static final String X_APP_USAGE = "X-App-Usage";
    private static final ObjectMapper USAGE_PARSER = new ObjectMapper();
    private final RestClient restClient;

    InstagramGraphClient(RestClient.Builder builder,
                         InstagramApiProperties properties) {
        this.restClient = builder
                .baseUrl(properties.getGraphBaseUrl())
                .build();
    }

    // ── Pojedyncze zapytanie GET ──────────────────────────────────────────────

    <T> T get(URI uri, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(responseType);

            checkAppUsage(response.getHeaders());
            return response.getBody();

        } catch (RestClientResponseException e) {
            throw parseError(e);
        }
    }

    // ── Paginacja cursor-based ────────────────────────────────────────────────

    <T, R> List<R> fetchAllPages(
            URI firstPageUri,
            Class<T> responseType,
            Function<T, List<R>> itemsExtractor,
            Function<T, String> nextCursorExtractor) {

        List<R> all     = new ArrayList<>();
        URI     current = firstPageUri;

        while (current != null) {
            T page = get(current, responseType);
            List<R> items = itemsExtractor.apply(page);
            if (items != null) all.addAll(items);

            String nextCursor = nextCursorExtractor.apply(page);
            current = nextCursor != null
                    ? appendAfterCursor(current, nextCursor)
                    : null;

            log.debug("Pobrano {} elementów, łącznie: {}",
                    items != null ? items.size() : 0, all.size());
        }

        return all;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    // ── rate limit ───────────────────────────────────────────────────────────

    private void checkAppUsage(HttpHeaders headers) {
        String raw = headers.getFirst(X_APP_USAGE);
        if (raw == null) return;

        try {
            AppUsage usage = USAGE_PARSER.readValue(raw, AppUsage.class);

            if (usage.isAtLimit()) {
                log.error("Instagram API rate limit osiągnięty: call={}% cpu={}% time={}%",
                        usage.callCount(), usage.totalCputime(), usage.totalTime());
                throw new InstagramApiException(rateLimitDetail(usage));
            }

            if (usage.isApproachingLimit()) {
                log.warn("Instagram API zbliża się do limitu: call={}% cpu={}% time={}%",
                        usage.callCount(), usage.totalCputime(), usage.totalTime());
            } else {
                log.debug("X-App-Usage: call={}% cpu={}% time={}%",
                        usage.callCount(), usage.totalCputime(), usage.totalTime());
            }

        } catch (InstagramApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Nie można sparsować X-App-Usage: {}", raw);
        }
    }

    private InstagramErrorResponse.ErrorDetail rateLimitDetail(AppUsage usage) {
        InstagramErrorResponse.ErrorDetail detail = new InstagramErrorResponse.ErrorDetail();
        detail.setCode(4);
        detail.setMessage("Rate limit osiągnięty: " + usage.maxUsage() + "%");
        detail.setType("OAuthException");
        detail.setFbtraceId("rate-limit");
        return detail;
    }

    private InstagramErrorResponse.ErrorDetail timeoutDetail(URI uri) {
        InstagramErrorResponse.ErrorDetail detail = new InstagramErrorResponse.ErrorDetail();
        detail.setCode(0);
        detail.setMessage("Read timeout: " + uri.getPath());
        detail.setType("NetworkError");
        detail.setFbtraceId("unknown");
        return detail;
    }

    private InstagramApiException parseError(RestClientResponseException e) {
        try {
            InstagramErrorResponse body =
                    e.getResponseBodyAs(InstagramErrorResponse.class);
            if (body != null && body.getError() != null) {
                return new InstagramApiException(body.getError());
            }
        } catch (Exception parseEx) {
            log.warn("Nie można sparsować błędu Instagram API: {}", e.getResponseBodyAsString());
        }
        // fallback — nieznany błąd
        InstagramErrorResponse.ErrorDetail fallback =
                new InstagramErrorResponse.ErrorDetail();
        fallback.setCode(e.getStatusCode().value());
        fallback.setMessage(e.getMessage());
        fallback.setType("UnknownError");
        fallback.setFbtraceId("unknown");
        return new InstagramApiException(fallback);
    }

    private URI appendAfterCursor(URI base, String cursor) {
        return UriComponentsBuilder.fromUri(base)
                .replaceQueryParam("after", cursor)
                .build().toUri();
    }
}