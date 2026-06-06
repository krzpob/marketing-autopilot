package pl.autopilot.datacollector.infrastructure.instagram.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;
import pl.autopilot.datacollector.domain.model.AccessToken;

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

    // ── buildAuthorizationUrl ────────────────────────────────────────────────

    @Test
    void buildAuthorizationUrl_shouldContainRequiredParams() {
        String url = client.buildAuthorizationUrl();

        assertThat(url)
                .contains("client_id=test-client-id")
                .contains("redirect_uri=")
                .contains("response_type=code")
                .contains("scope=");
    }

    // ── exchangeCodeForShortLivedToken ───────────────────────────────────────
        @Test
        void exchangeCode_shouldReturnShortLivedToken() {
                wireMock.stubFor(post(urlPathEqualTo("/v19.0/oauth/access_token"))
                        .willReturn(okJson("""
                                {"access_token":"short-lived-123","token_type":"bearer","expires_in":3600}
                                """)));

                wireMock.stubFor(get(urlPathEqualTo("/me"))
                        .withQueryParam("fields", equalTo("id,name"))
                        .willReturn(okJson("""
                                {"id":"12345678","name":"testuser"}
                                """)));

                AccessToken token = client.exchangeCodeForShortLivedToken("auth-code-abc");

                assertThat(token.getToken()).isEqualTo("short-lived-123");
                assertThat(token.getOwnerIgId()).isEqualTo("12345678");
                assertThat(token.getOwnerUsername()).isEqualTo("testuser");
                assertThat(token.getTokenType()).isEqualTo(AccessToken.TokenType.SHORT_LIVED);
                assertThat(token.getExpiresAt()).isNotNull();
        }

    @Test
    void exchangeCode_whenApiFails_shouldThrow() {
        wireMock.stubFor(post(urlPathEqualTo("/v19.0/oauth/access_token"))
                .willReturn(aResponse().withStatus(400).withBody("""
                        {"error":{"message":"Invalid OAuth code","code":100}}
                        """)));

        assertThatThrownBy(() -> client.exchangeCodeForShortLivedToken("invalid-code"))
                .isInstanceOf(Exception.class);
    }

    // ── exchangeForLongLivedToken ────────────────────────────────────────────

    @Test
    void exchangeForLongLived_shouldReturnLongLivedToken() {
        wireMock.stubFor(get(urlPathEqualTo("/v19.0/oauth/access_token"))
                .withQueryParam("grant_type", equalTo("fb_exchange_token"))
                .willReturn(okJson("""
                        {"access_token":"long-lived-xyz","token_type":"bearer","expires_in":5184000}
                        """)));

        AccessToken shortLived = AccessToken.builder()
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .token("short-lived-123")
                .tokenType(AccessToken.TokenType.SHORT_LIVED)
                .build();

        AccessToken longLived = client.exchangeForLongLivedToken(shortLived);

        assertThat(longLived.getToken()).isEqualTo("long-lived-xyz");
        assertThat(longLived.getTokenType()).isEqualTo(AccessToken.TokenType.LONG_LIVED);
        assertThat(longLived.getOwnerIgId()).isEqualTo("12345678");
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