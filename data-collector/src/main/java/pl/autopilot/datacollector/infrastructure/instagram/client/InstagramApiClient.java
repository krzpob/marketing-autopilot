package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.infrastructure.instagram.mapper.InstagramMediaMapper;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;


import java.net.URI;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstagramApiClient {

    private static final String MEDIA_FIELDS =
            "id,shortcode,media_type,caption,media_url,permalink," +
            "like_count,comments_count,timestamp";

    private final InstagramGraphClient    graphClient;
    private final InstagramApiProperties  properties;
    private final InstagramMediaMapper    mediaMapper;

    // ── B2-06: GET /me/media ─────────────────────────────────────────────────

    public List<CollectedPost> fetchOwnMedia(AccessToken token) {
        Objects.requireNonNull(token, "AccessToken must not be null");
        if (!StringUtils.hasText(token.getOwnerIgId())) {
            throw new IllegalArgumentException("AccessToken.ownerIgId must not be blank");
        }
        URI firstPage = UriComponentsBuilder
                .fromUriString(properties.getGraphBaseUrl())
                .path("/{igUserId}/media")
                .queryParam("fields",       MEDIA_FIELDS)
                .queryParam("limit",        25)
                .queryParam("access_token", token.getToken())
                .buildAndExpand(token.getOwnerIgId())
                .toUri();

        List<CollectedPost> posts = graphClient.fetchAllPages(
                firstPage,
                InstagramMediaResponse.class,
                InstagramMediaResponse::getData,
                response -> extractNextCursor(response)
        ).stream()
         .map(item -> mediaMapper.toDomain(item,
                 token.getOwnerIgId(), token.getOwnerUsername()))
         .toList();

        log.info("Pobrano {} postów dla ownerIgId={}", posts.size(), token.getOwnerIgId());
        return posts;
    }

    // ── B2-07 / B2-08 — stubs ───────────────────────────────────────────────

    public InstagramMediaResponse fetchCompetitorMedia(String igUserId,
                                                       String accessToken) {
        // TODO: B2-07
        return new InstagramMediaResponse();
    }

    public String findHashtagId(String hashtag, String accessToken) {
        // TODO: B2-08
        return null;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String extractNextCursor(InstagramMediaResponse response) {
        if (response.getPaging() == null) return null;
        if (response.getPaging().getNext() == null) return null;
        if (response.getPaging().getCursors() == null) return null;
        return response.getPaging().getCursors().getAfter();
    }
}