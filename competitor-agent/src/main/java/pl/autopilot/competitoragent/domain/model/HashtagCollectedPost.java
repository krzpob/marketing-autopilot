package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class HashtagCollectedPost {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String igMediaId;
    private final String hashtag;       // hashtag, który go zebrał
    private final String igHashtagId;

    private final MediaType mediaType;
    private final String    permalink;
    private final String    caption;    // nullable
    private final List<String> hashtags; // hashtagi w treści posta

    private final long likeCount;
    private final int  commentsCount;

    private final Instant publishedAt;

    @Builder.Default
    private final Instant collectedAt = Instant.now();

    public enum MediaType {
        IMAGE, VIDEO, CAROUSEL_ALBUM, REEL, UNKNOWN
    }
}