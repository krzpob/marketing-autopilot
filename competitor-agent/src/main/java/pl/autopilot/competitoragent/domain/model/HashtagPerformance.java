package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class HashtagPerformance {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String hashtag;
    private final String igHashtagId;

    // ── metryki z HashtagDataEvent ────────────────────────────────────────────
    private final int  topMediaCount;       // ile postów w top/recent media
    private final long avgLikeCount;        // średnia likes w zebranych postach
    private final int  avgCommentsCount;    // średnia komentarzy

    // ── trend ─────────────────────────────────────────────────────────────────
    private final TrendDirection trend;
    private final double         trendScore; // zmiana avgLikeCount vs poprzednia kolekcja

    @Builder.Default
    private final Instant collectedAt = Instant.now();
    private final Instant previousCollectedAt; // nullable — null przy pierwszej kolekcji

    public enum TrendDirection {
        RISING,   // trendScore > +10%
        STABLE,   // trendScore ±10%
        FALLING   // trendScore < -10%
    }

    public static TrendDirection classify(double trendScore) {
        if (trendScore > 10)  return TrendDirection.RISING;
        if (trendScore < -10) return TrendDirection.FALLING;
        return TrendDirection.STABLE;
    }
}