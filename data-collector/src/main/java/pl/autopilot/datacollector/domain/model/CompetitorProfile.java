package pl.autopilot.datacollector.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class CompetitorProfile {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String igId;              // Instagram user ID
    private final String username;
    private final long followerCount;
    private final int mediaCount;
    private final String biography;         // nullable
    private final String website;           // nullable
    private final String profilePictureUrl; // nullable
    private final boolean businessAccount;

    @Builder.Default
    private final Instant collectedAt = Instant.now();

    private final Instant lastCollectedAt;  // nullable — null przy pierwszym pobraniu
}