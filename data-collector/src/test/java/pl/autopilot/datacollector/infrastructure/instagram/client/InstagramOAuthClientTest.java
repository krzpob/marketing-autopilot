package pl.autopilot.datacollector.infrastructure.instagram.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import pl.autopilot.datacollector.domain.model.AccessToken;

import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class InstagramOAuthClientTest {

    private static WireMockServer wireMock;
    private InstagramOAuthClient client;

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

        String base = "http://localhost:" + wireMock.port();

        InstagramApiProperties props = new InstagramApiProperties();
        props.setClientId("test-client-id");
        props.setClientSecret("test-client-secret");
        props.setRedirectUri("http://localhost/oauth/instagram/callback");
        props.setGraphBaseUrl(base);
        props.setAuthBaseUrl(base + "/dialog/oauth");
        props.setTokenBaseUrl(base + "/v19.0/oauth/access_token");

        client = new InstagramOAuthClient(RestClient.builder(), props);
    }

    // ── exchangeForLongLivedToken ────────────────────────────────────────────

    @Test
    void exchangeForLongLived_shouldReturnLongLivedToken() {
        wireMock.stubFor(get(urlPathEqualTo("/v19.0/oauth/access_token"))
                .withQueryParam("grant_type", equalTo("fb_exchange_token"))
                .willReturn(okJson("""
                        {"access_token":"long-lived-xyz","token_type":"bearer","expires_in":5184000}
                        """)));

        AccessToken longLived = client.exchangeForLongLivedToken("short-lived-123");

        assertThat(longLived.getToken()).isEqualTo("long-lived-xyz");
        assertThat(longLived.getTokenType()).isEqualTo(AccessToken.TokenType.LONG_LIVED);
        // assertThat(longLived.getOwnerIgId()).isEqualTo("12345678");
        assertThat(longLived.getExpiresAt()).isAfter(longLived.getCreatedAt());

        // weryfikacja że WireMock dostał poprawne parametry
        wireMock.verify(getRequestedFor(urlPathEqualTo("/v19.0/oauth/access_token"))
                .withQueryParam("client_id",     equalTo("test-client-id"))
                .withQueryParam("client_secret", equalTo("test-client-secret"))
                .withQueryParam("fb_exchange_token", equalTo("short-lived-123")));
    }

    // ── refreshLongLivedToken ────────────────────────────────────────────────

    @Test
    void refresh_shouldUpdateTokenAndRefreshedAt() {
        wireMock.stubFor(get(urlPathEqualTo("/v19.0/oauth/access_token"))
                .withQueryParam("grant_type", equalTo("fb_exchange_token"))
                .willReturn(okJson("""
                        {"access_token":"refreshed-999","token_type":"bearer","expires_in":5184000}
                        """)));

        AccessToken existing = AccessToken.builder()
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .token("old-long-lived-token")
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .build();

        AccessToken refreshed = client.refreshLongLivedToken(existing);

        assertThat(refreshed.getToken()).isEqualTo("refreshed-999");
        assertThat(refreshed.getOwnerIgId()).isEqualTo("12345678");
        assertThat(refreshed.getRefreshedAt()).isNotNull();
        // id powinno zostać to samo
        assertThat(refreshed.getId()).isEqualTo(existing.getId());
    }
}