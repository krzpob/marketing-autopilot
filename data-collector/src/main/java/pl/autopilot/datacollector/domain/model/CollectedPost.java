package pl.autopilot.datacollector.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class CollectedPost {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String shortcode;
    private final String ownerIgId;
    private final String ownerUsername;
    private final MediaType mediaType;
    private final String caption;           // nullable
    private final List<String> hashtags;
    private final List<String> mentions;
    private final String mediaUrl;          // nullable
    private final String permalink;
    private final long likeCount;
    private final int commentsCount;
    private final int shareCount;
    private final Instant publishedAt;

    @Builder.Default
    private final Instant collectedAt = Instant.now();

    public enum MediaType {
        IMAGE, VIDEO, CAROUSEL_ALBUM, REEL, UNKNOWN
    }

    /** Engagement rate = (likes + comments) / followers * 100 */
    public double engagementRate(long ownerFollowerCount) {
        if (ownerFollowerCount == 0) return 0;
        return (likeCount + commentsCount) * 100.0 / ownerFollowerCount;
    }
}