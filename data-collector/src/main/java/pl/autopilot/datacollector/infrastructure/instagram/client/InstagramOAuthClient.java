package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramTokenResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramUserResponse;

import java.time.Instant;
import java.util.UUID;
import java.net.URI;

@Slf4j
@Component
public class InstagramOAuthClient {

    private final WebClient webClient;
    private final InstagramApiProperties properties;

    public InstagramOAuthClient(WebClient.Builder builder,
                                InstagramApiProperties properties) {
        this.webClient  = builder.build();
        this.properties = properties;
    }

    // ── B2-03: Authorization URL ─────────────────────────────────────────────

    public String buildAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        return UriComponentsBuilder.fromUriString(properties.getAuthBaseUrl())
                .queryParam("client_id",     properties.getClientId())
                .queryParam("redirect_uri",  properties.getRedirectUri())
                .queryParam("scope",         properties.getScopes())
                .queryParam("response_type", "code")
                .queryParam("state",         state)
                .toUriString();
    }

    // ── B2-03: Exchange code → short-lived token ─────────────────────────────

    public AccessToken exchangeCodeForShortLivedToken(String code) {
        InstagramTokenResponse response = webClient.post()
                .uri(properties.getTokenBaseUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(buildCodeExchangeBody(code))
                .retrieve()
                .bodyToMono(InstagramTokenResponse.class)
                .block();

        InstagramUserResponse user = fetchMe(response.getAccessToken());

        log.info("Short-lived token uzyskany dla użytkownika: {}", user.getUsername());

        return AccessToken.builder()
                .ownerIgId(user.getId())
                .ownerUsername(user.getUsername())
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
                    .build()
                    .toUri();
        InstagramTokenResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(InstagramTokenResponse.class)
                .block();

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
                    .build()
                    .toUri();
        InstagramTokenResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(InstagramTokenResponse.class)
                .block();

        log.info("Token odświeżony dla: {}", longLived.getOwnerUsername());

        return longLived.withRefreshed(
                response.getAccessToken(),
                Instant.now().plusSeconds(response.getExpiresIn())
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private InstagramUserResponse fetchMe(String accessToken) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getGraphBaseUrl())
                    .path("/me")
                    .queryParam("fields",       "id,username")
                    .queryParam("access_token", accessToken)
                    .build()
                    .toUri();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(InstagramUserResponse.class)
                .block();
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