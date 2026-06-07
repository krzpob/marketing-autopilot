package pl.autopilot.datacollector.infrastructure.instagram.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

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
                props,
                new ObjectMapper()
        );
    }

    // ── paginacja ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnAllItemsFromSinglePage() {
        // given
        givenThat(get(urlPathEqualTo("/me/media"))
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
        givenThat(get(urlPathEqualTo("/me/media"))
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

        givenThat(get(urlPathEqualTo("/me/media"))
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
        givenThat(get(urlPathEqualTo("/me/media"))
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
        givenThat(get(urlPathEqualTo("/me/media"))
                .willReturn(aResponse().withStatus(400).withBody("""
                        {
                          "error": {
                            "message": "Invalid parameter",
                            "type": "GraphMethodException",
                            "code": 100,
                            "fbtrace_id": "trace123"
                          }
                        }
                        """)));

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
        givenThat(get(urlPathEqualTo("/me/media"))
                .willReturn(aResponse().withStatus(401).withBody("""
                        {
                          "error": {
                            "message": "Error validating access token",
                            "type": "OAuthException",
                            "code": 190,
                            "fbtrace_id": "trace456"
                          }
                        }
                        """)));

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