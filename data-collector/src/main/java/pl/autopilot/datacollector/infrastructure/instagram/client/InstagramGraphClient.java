package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramErrorResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
class InstagramGraphClient {

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
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            throw parseError(e);
        } catch (ResourceAccessException e) {
            log.error("Timeout lub błąd sieci dla URI: {}", uri);
            throw new InstagramApiException(timeoutError(uri));
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

    private InstagramErrorResponse.ErrorDetail timeoutError(URI uri) {
        InstagramErrorResponse.ErrorDetail detail = new InstagramErrorResponse.ErrorDetail();
        detail.setCode(0);
        detail.setMessage("Read timeout: " + uri.getPath());
        detail.setType("NetworkError");
        detail.setFbtraceId("unknown");
        return detail;
    }
}