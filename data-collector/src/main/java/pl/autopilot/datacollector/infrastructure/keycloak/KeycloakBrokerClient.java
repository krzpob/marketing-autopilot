package pl.autopilot.datacollector.infrastructure.keycloak;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakBrokerClient {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;

    public String getFacebookToken(String keycloakJwt) {
        return restClient.get()
                .uri(keycloakProperties.brokerTokenUrl())
                .header("Authorization", "Bearer " + keycloakJwt)
                .retrieve()
                .body(KeycloakBrokerTokenResponse.class)
                .accessToken();
    }

    public record KeycloakBrokerTokenResponse(
    @JsonProperty("access_token") String accessToken
) {}
}