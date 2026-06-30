package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class CompetitorProfile {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String igId;
    private final String username;
    private final long   followerCount;
    private final int    mediaCount;
    private final String biography;         // nullable

    // ── średnia krocząca ER ───────────────────────────────────────────────────
    private final double  rollingAvgEngagementRate;  // ostatnie N postów
    private final int     rollingWindowSize;          // ile postów w oknie
    private final Instant rollingAvgUpdatedAt;        // nullable — null przy pierwszym

    @Builder.Default
    private final Instant updatedAt = Instant.now();
}