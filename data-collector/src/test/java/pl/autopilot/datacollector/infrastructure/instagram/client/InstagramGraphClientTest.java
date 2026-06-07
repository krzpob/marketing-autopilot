package pl.autopilot.datacollector.infrastructure.instagram.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

class InstagramGraphClientTest {

    private static WireMockServer wireMock;
    private InstagramGraphClient  graphClient;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();

        InstagramApiProperties props = new InstagramApiProperties();
        props.setGraphBaseUrl("http://localhost:" + wireMock.port());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(2));

        graphClient = new InstagramGraphClient(
                RestClient.builder().requestFactory(factory),
                props
        );
    }

    // ── paginacja ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnAllItemsFromSinglePage() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/me/media"))
                .willReturn(okJson("""
                        {
                          "data": [
                            {"id":"1","shortcode":"abc"},
                            {"id":"2","shortcode":"def"}
                          ],
                          "paging": {}
                        }
                        """)));

        // when
        List<InstagramMediaResponse.MediaItem> items = graphClient.fetchAllPages(
                URI.create("http://localhost:" + wireMock.port() + "/me/media"),
                InstagramMediaResponse.class,
                InstagramMediaResponse::getData,
                r -> extractCursor(r)
        );

        // then
        then(items).hasSize(2);
        then(items).extracting(InstagramMediaResponse.MediaItem::getShortcode)
                .containsExactly("abc", "def");
    }

    @Test
    void shouldFetchAllPagesUntilNoCursor() {
        // given — strona 1 zwraca cursor, strona 2 nie ma kolejnej
        wireMock.stubFor(get(urlPathEqualTo("/me/media"))
                .withQueryParam("after", absent())
                .willReturn(okJson("""
                        {
                          "data": [{"id":"1","shortcode":"p1"}],
                          "paging": {
                            "cursors": {"after":"cursor_abc","before":"x"},
                            "next": "http://someurl"
                          }
                        }
                        """)));

        wireMock.stubFor(get(urlPathEqualTo("/me/media"))
                .withQueryParam("after", equalTo("cursor_abc"))
                .willReturn(okJson("""
                        {
                          "data": [{"id":"2","shortcode":"p2"}],
                          "paging": {"cursors": {"before":"y"}}
                        }
                        """)));

        // when
        List<InstagramMediaResponse.MediaItem> items = graphClient.fetchAllPages(
                URI.create("http://localhost:" + wireMock.port() + "/me/media"),
                InstagramMediaResponse.class,
                InstagramMediaResponse::getData,
                r -> extractCursor(r)
        );

        // then
        then(items).hasSize(2);
        then(items).extracting(InstagramMediaResponse.MediaItem::getShortcode)
                .containsExactly("p1", "p2");

        wireMock.verify(2, getRequestedFor(urlPathEqualTo("/me/media")));
    }

    @Test
    void shouldHandleEmptyDataList() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/me/media"))
                .willReturn(okJson("""
                        {"data": [], "paging": {}}
                        """)));

        // when
        List<InstagramMediaResponse.MediaItem> items = graphClient.fetchAllPages(
                URI.create("http://localhost:" + wireMock.port() + "/me/media"),
                InstagramMediaResponse.class,
                InstagramMediaResponse::getData,
                r -> extractCursor(r)
        );

        // then
        then(items).isEmpty();
    }

    // ── obsługa błędów ────────────────────────────────────────────────────────

    @Test
    void shouldThrowInstagramApiExceptionOn400() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/me/media"))
        .willReturn(jsonResponse("""
                {
                  "error": {
                    "message": "Invalid parameter",
                    "type": "GraphMethodException",
                    "code": 100,
                    "fbtrace_id": "trace123"
                  }
                }
                """, 400)));

        // when / then
        thenThrownBy(() -> graphClient.get(
                URI.create("http://localhost:" + wireMock.port() + "/me/media"),
                InstagramMediaResponse.class))
                .isInstanceOf(InstagramApiException.class)
                .hasMessageContaining("100")
                .hasMessageContaining("Invalid parameter");
    }

    @Test
    void shouldThrowWithExpiredTokenCode() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/me/media"))
        .willReturn(jsonResponse("""
                {
                  "error": {
                    "message": "Error validating access token",
                    "type": "OAuthException",
                    "code": 190,
                    "fbtrace_id": "trace456"
                  }
                }
                """, 401)));

        // when / then
        thenThrownBy(() -> graphClient.get(
                URI.create("http://localhost:" + wireMock.port() + "/me/media"),
                InstagramMediaResponse.class))
                .isInstanceOf(InstagramApiException.class)
                .matches(e -> ((InstagramApiException) e).isTokenExpired());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private String extractCursor(InstagramMediaResponse r) {
        if (r.getPaging() == null)          return null;
        if (r.getPaging().getNext() == null) return null;
        if (r.getPaging().getCursors() == null) return null;
        return r.getPaging().getCursors().getAfter();
    }
}