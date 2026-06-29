package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramTokenResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramUserResponse;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

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

    // ── B2-03: Authorization URL ─────────────────────────────────────────────

    public String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString(properties.getAuthBaseUrl())
                .queryParam("client_id",     properties.getClientId())
                .queryParam("redirect_uri",  properties.getRedirectUri())
                .queryParam("scope",         properties.getScopes())
                .queryParam("response_type", "code")
                .queryParam("state",         UUID.randomUUID().toString())
                .toUriString();
    }

    // ── B2-03: Exchange code → short-lived token ─────────────────────────────

    public AccessToken exchangeCodeForShortLivedToken(String code) {
        InstagramTokenResponse response = restClient.post()
                .uri(properties.getTokenBaseUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildCodeExchangeBody(code))
                .retrieve()
                .body(InstagramTokenResponse.class);

        InstagramUserResponse user = fetchMe(response.getAccessToken());

        String igId       = user.getInstagramAccountId();
        String igUsername = user.getInstagramUsername();

        if (igId == null) {
                log.warn("Brak Instagram Business Account dla Facebook User: {}", user.getId());
                // fallback na Facebook User ID — przyda się do debugowania
                igId       = user.getId();
                igUsername = user.getName();
        }
        log.info("Short-lived token dla Instagram: {} ({})", igUsername, igId);

        return AccessToken.builder()
                .ownerIgId(igId)
                .ownerUsername(igUsername)
                .token(response.getAccessToken())
                .tokenType(AccessToken.TokenType.SHORT_LIVED)
                .expiresAt(Instant.now().plusSeconds(3_600))
                .build();
        }       

    // ── B2-04: Exchange short-lived → long-lived token ───────────────────────

    public AccessToken exchangeForLongLivedToken(AccessToken shortLived) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getTokenBaseUrl())
                .queryParam("grant_type",        "fb_exchange_token")
                .queryParam("client_id",         properties.getClientId())
                .queryParam("client_secret",     properties.getClientSecret())
                .queryParam("fb_exchange_token", shortLived.getToken())
                .build().toUri();

        InstagramTokenResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(InstagramTokenResponse.class);

        log.info("Long-lived token uzyskany dla: {}", shortLived.getOwnerUsername());

        return shortLived.toBuilder()
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

    // ── helpers ──────────────────────────────────────────────────────────────

    private InstagramUserResponse fetchMe(String accessToken) {
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

    private MultiValueMap<String, String> buildCodeExchangeBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id",     properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("grant_type",    "authorization_code");
        body.add("redirect_uri",  properties.getRedirectUri());
        body.add("code",          code);
        return body;
    }
}