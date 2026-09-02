package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramTokenResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramUserResponse;

import java.net.URI;
import java.time.Instant;

@Slf4j
@Component
public class InstagramOAuthClient {

    private final RestClient restClient;
    private final InstagramApiProperties properties;

    public InstagramOAuthClient(RestClient.Builder builder,
                                InstagramApiProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }    

    // ── B2-04: Exchange short-lived → long-lived token ───────────────────────

    public AccessToken exchangeForLongLivedToken(String shortLived) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getTokenBaseUrl())
                .queryParam("grant_type",        "fb_exchange_token")
                .queryParam("client_id",         properties.getClientId())
                .queryParam("client_secret",     properties.getClientSecret())
                .queryParam("fb_exchange_token", shortLived)
                .build().toUri();

        InstagramTokenResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(InstagramTokenResponse.class);

        log.info("Long-lived token uzyskany dla]");

        return AccessToken.builder()
            .token(response.getAccessToken())
            .tokenType(AccessToken.TokenType.LONG_LIVED)
            .expiresAt(Instant.now().plusSeconds(response.getExpiresIn()))
            .build();
    }

    // ── B2-05: Refresh long-lived token ─────────────────────────────────────

    public AccessToken refreshLongLivedToken(AccessToken longLived) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getTokenBaseUrl())
                .queryParam("grant_type",        "fb_exchange_token")
                .queryParam("client_id",         properties.getClientId())
                .queryParam("client_secret",     properties.getClientSecret())
                .queryParam("fb_exchange_token", longLived.getToken())
                .build().toUri();

        InstagramTokenResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(InstagramTokenResponse.class);

        log.info("Token odświeżony dla: {}", longLived.getOwnerUsername());

        return longLived.toBuilder()
                .token(response.getAccessToken())
                .expiresAt(Instant.now().plusSeconds(response.getExpiresIn()))
                .refreshedAt(Instant.now())
                .build();
    }

    public InstagramUserResponse fetchMe(String accessToken) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getGraphBaseUrl())
                .path("/me")
                .queryParam("fields",  
                     "id,name,accounts{instagram_business_account{id,username}}")
                .queryParam("access_token", accessToken)
                .build().toUri();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(InstagramUserResponse.class);
    }

}