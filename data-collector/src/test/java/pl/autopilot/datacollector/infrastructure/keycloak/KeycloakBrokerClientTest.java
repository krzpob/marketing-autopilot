package pl.autopilot.datacollector.infrastructure.keycloak;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import pl.autopilot.datacollector.infrastructure.keycloak.KeycloakBrokerClient;
import pl.autopilot.datacollector.infrastructure.keycloak.KeycloakProperties;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class KeycloakBrokerClientTest {

    private static WireMockServer wireMock;
    private KeycloakBrokerClient client;

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
        KeycloakProperties props = new KeycloakProperties(
                base + "/realms/marketing-autopilot/broker/facebook/token"
        );
        client = new KeycloakBrokerClient(RestClient.builder().build(), props);
    }

    @Test
    void getFacebookToken_shouldReturnAccessToken() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/realms/marketing-autopilot/broker/facebook/token"))
                .withHeader("Authorization", equalTo("Bearer keycloak-jwt-123"))
                .willReturn(jsonResponse("""
                        {"access_token":"fb-token-abc","token_type":"Bearer","expires_in":3600}
                        """, 200)));

        // when
        String token = client.getFacebookToken("keycloak-jwt-123");

        // then
        assertThat(token).isEqualTo("fb-token-abc");

        wireMock.verify(getRequestedFor(
                urlPathEqualTo("/realms/marketing-autopilot/broker/facebook/token"))
                .withHeader("Authorization", equalTo("Bearer keycloak-jwt-123")));
    }

    @Test
    void getFacebookToken_shouldThrow_whenKeycloakReturns401() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/realms/marketing-autopilot/broker/facebook/token"))
                .willReturn(jsonResponse("{\"error\":\"invalid_token\"}", 401)));

        // when / then
        assertThatThrownBy(() -> client.getFacebookToken("invalid-jwt"))
                .isInstanceOf(HttpClientErrorException.class);
    }
}
