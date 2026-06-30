package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class EngagementAnalysis {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    // ── kontekst ──────────────────────────────────────────────────────────────
    private final String igMediaId;
    private final String competitorUsername;

    // ── wyniki ────────────────────────────────────────────────────────────────
    private final double engagementRate;        // (likes + comments) / followers * 100
    private final double deltaVsRollingAvg;     // ER posta - średnia krocząca (+ lepszy, - gorszy)

    private final EngagementLevel level;        // kategoryzacja

    @Builder.Default
    private final Instant analyzedAt = Instant.now();

    public enum EngagementLevel {
        LOW,      // poniżej średniej kroczącej
        AVERAGE,  // ±20% średniej kroczącej
        HIGH,     // powyżej średniej kroczącej
        VIRAL     // 2x powyżej średniej kroczącej
    }

    public static EngagementLevel classify(double er, double rollingAvg) {
        if (rollingAvg == 0) return EngagementLevel.AVERAGE;
        double ratio = er / rollingAvg;
        if (ratio >= 2.0)  return EngagementLevel.VIRAL;
        if (ratio >= 1.2)  return EngagementLevel.HIGH;
        if (ratio >= 0.8)  return EngagementLevel.AVERAGE;
        return EngagementLevel.LOW;
    }
}