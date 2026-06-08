package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.infrastructure.instagram.mapper.InstagramMediaMapper;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramHashtagResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramHashtagStatsResponse;
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

    private static final String HASHTAG_FIELDS = "id,name";

    private static final String HASHTAG_MEDIA_FIELDS = "id,media_type,permalink,like_count,comments_count,timestamp";


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

    // ── B2-08: GET /ig_hashtag_search ────────────────────────────────────────

    public HashtagStats fetchHashtagStats(String hashtag, AccessToken token) {
        Objects.requireNonNull(token,   "AccessToken must not be null");
        Objects.requireNonNull(hashtag, "Hashtag must not be null");

        String igHashtagId = findHashtagId(hashtag, token);
        if (igHashtagId == null) {
            log.warn("Nie znaleziono hashtagу: #{}", hashtag);
            return null;
        }

        URI uri = UriComponentsBuilder
                .fromUriString(properties.getGraphBaseUrl())
                .path("/{hashtagId}")
                .queryParam("fields",       HASHTAG_FIELDS)
                .queryParam("access_token", token.getToken())
                .buildAndExpand(igHashtagId)
                .toUri();

        InstagramHashtagStatsResponse response =
                graphClient.get(uri, InstagramHashtagStatsResponse.class);

        log.info("Hashtag #{}", hashtag);

        return HashtagStats.builder()
                .hashtag(hashtag.toLowerCase().replace("#", ""))
                .igHashtagId(igHashtagId)
                .mediaCount(0)
                .build();
    }

    public String findHashtagId(String hashtag, AccessToken token) {
        URI uri = UriComponentsBuilder
                .fromUriString(properties.getGraphBaseUrl())
                .path("/ig_hashtag_search")
                .queryParam("q",            hashtag.replace("#", ""))
                .queryParam("user_id",      token.getOwnerIgId())
                .queryParam("access_token", token.getToken())
                .build().toUri();

        InstagramHashtagResponse response =
                graphClient.get(uri, InstagramHashtagResponse.class);

        if (response.getData() == null || response.getData().isEmpty()) {
            return null;
        }
        return response.getData().get(0).getId();
    }

    public List<CollectedPost> fetchHashtagTopMedia(String hashtag, AccessToken token) {
    Objects.requireNonNull(token,   "AccessToken must not be null");
    Objects.requireNonNull(hashtag, "Hashtag must not be null");

    String igHashtagId = findHashtagId(hashtag, token);
    if (igHashtagId == null) {
        log.warn("Nie znaleziono hashtagу: #{}", hashtag);
        return List.of();
    }

    URI uri = UriComponentsBuilder
            .fromUriString(properties.getGraphBaseUrl())
            .path("/{hashtagId}/top_media")
            .queryParam("fields",       HASHTAG_MEDIA_FIELDS)
            .queryParam("user_id",      token.getOwnerIgId())
            .queryParam("limit",    20)
            .queryParam("access_token", token.getToken())
            .buildAndExpand(igHashtagId)
            .toUri();

    log.info("First page url: {}", uri.toASCIIString());

    InstagramMediaResponse response = graphClient.get(uri, InstagramMediaResponse.class);

    List<CollectedPost> posts = response.getData() == null
            ? List.of()
            : response.getData().stream()
                      .map(item -> mediaMapper.toDomain(item, igHashtagId, hashtag))
                      .toList();

    log.info("Pobrano {} top postów dla #{}", posts.size(), hashtag);
    return posts;
}

    // ── helpers ──────────────────────────────────────────────────────────────

    private String extractNextCursor(InstagramMediaResponse response) {
        if (response.getPaging() == null) return null;
        if (response.getPaging().getNext() == null) return null;
        if (response.getPaging().getCursors() == null) return null;
        return response.getPaging().getCursors().getAfter();
    }
}