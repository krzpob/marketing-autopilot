package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class CompetitorPost {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    // ── identyfikacja ─────────────────────────────────────────────────────────
    private final String igMediaId;         // Instagram media ID
    private final String shortcode;
    private final String competitorUsername;
    private final String ownerIgId;         // czyj token zebrał dane

    // ── content ───────────────────────────────────────────────────────────────
    private final MediaType mediaType;
    private final String    caption;        // nullable
    private final List<String> hashtags;
    private final String    mediaUrl;       // nullable — do analizy treści karuzeli

    // ── metryki ───────────────────────────────────────────────────────────────
    private final long likeCount;
    private final int  commentsCount;
    private final long followerCountAtCollection; // przybliżenie — stan w chwili pobrania

    // ── timestampy ────────────────────────────────────────────────────────────
    private final Instant publishedAt;
    private final Instant collectedAt;

    public enum MediaType {
        IMAGE, VIDEO, CAROUSEL_ALBUM, REEL, UNKNOWN;

        public String toPermalink(String shortcode) {
            return switch (this) {
                case REEL -> "https://www.instagram.com/reel/" + shortcode + "/";
                default   -> "https://www.instagram.com/p/" + shortcode + "/";
            };
        }
    }

    public String permalink() {
        return mediaType.toPermalink(shortcode);
    }

    public double engagementRate() {
        if (followerCountAtCollection == 0) return 0;
        return (likeCount + commentsCount) * 100.0 / followerCountAtCollection;
    }
}