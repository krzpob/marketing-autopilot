package pl.autopilot.datacollector.infrastructure.instagram.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramErrorResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class InstagramGraphClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public InstagramGraphClient(RestClient.Builder builder,
                                InstagramApiProperties properties,
                                ObjectMapper objectMapper) {
        this.restClient   = builder
                .baseUrl(properties.getGraphBaseUrl())
                .defaultStatusHandler(
                        status -> status.isError(),
                        (req, res) -> {
                            InstagramErrorResponse err = objectMapper
                                    .readValue(res.getBody(), InstagramErrorResponse.class);
                            throw new InstagramApiException(err.getError());
                        })
                .build();
        this.objectMapper = objectMapper;
    }

    // ── Pojedyncze zapytanie GET ──────────────────────────────────────────────

    public <T> T get(URI uri, Class<T> responseType) {
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(responseType);
    }

    // ── Paginacja cursor-based ────────────────────────────────────────────────

    /**
     * Pobiera wszystkie strony wyników z Instagram API.
     * Instagram używa cursor-based pagination: każda strona zwraca
     * paging.cursors.after i paging.next gdy są kolejne wyniki.
     *
     * @param firstPageUri URI pierwszej strony
     * @param responseType typ odpowiedzi (musi mieć pole paging)
     * @param itemsExtractor funkcja wyciągająca listę elementów z odpowiedzi
     * @param nextUriBuilder funkcja budująca URI następnej strony z cursora
     */
    public <T, R> List<R> fetchAllPages(
            URI firstPageUri,
            Class<T> responseType,
            Function<T, List<R>> itemsExtractor,
            Function<T, String> nextCursorExtractor) {

        List<R> all    = new ArrayList<>();
        URI     current = firstPageUri;

        while (current != null) {
            T page = get(current, responseType);
            List<R> items = itemsExtractor.apply(page);

            if (items != null) all.addAll(items);

            String nextCursor = nextCursorExtractor.apply(page);
            current = nextCursor != null
                    ? appendAfterCursor(current, nextCursor)
                    : null;

            log.debug("Pobrano {} elementów, łącznie: {}, kolejna strona: {}",
                    items != null ? items.size() : 0, all.size(), current != null);
        }

        return all;
    }

    private URI appendAfterCursor(URI base, String cursor) {
        return UriComponentsBuilder.fromUri(base)
                .replaceQueryParam("after", cursor)
                .build().toUri();
    }
}