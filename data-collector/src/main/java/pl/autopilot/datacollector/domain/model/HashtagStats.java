package pl.autopilot.datacollector.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class HashtagStats {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String hashtag;           // bez #, np. "fotografia"
    private final String igHashtagId;       // Instagram wewnętrzne ID hashtagу
    private final long mediaCount;          // liczba postów z tym hashtagiem

    @Builder.Default
    private final Instant collectedAt = Instant.now();
}