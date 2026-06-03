package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

@Slf4j
@Component
public class InstagramApiClient {

    private final WebClient webClient;
    private final InstagramApiProperties properties;

    public InstagramApiClient(WebClient.Builder builder,
                              InstagramApiProperties properties) {
        this.webClient  = builder.baseUrl(properties.getGraphBaseUrl()).build();
        this.properties = properties;
    }

    // ── B2-06: GET /me/media ─────────────────────────────────────────────────

    public InstagramMediaResponse fetchOwnMedia(String accessToken) {
        // TODO: B2-06
        log.info("TODO: fetchOwnMedia");
        return new InstagramMediaResponse();
    }

    // ── B2-07: Business Discovery GET /{user-id}/media ───────────────────────

    public InstagramMediaResponse fetchCompetitorMedia(String igUserId,
                                                       String accessToken) {
        // TODO: B2-07
        log.info("TODO: fetchCompetitorMedia igUserId={}", igUserId);
        return new InstagramMediaResponse();
    }

    // ── B2-08: GET /ig_hashtag_search ────────────────────────────────────────

    public String findHashtagId(String hashtag, String accessToken) {
        // TODO: B2-08
        log.info("TODO: findHashtagId hashtag={}", hashtag);
        return null;
    }
}