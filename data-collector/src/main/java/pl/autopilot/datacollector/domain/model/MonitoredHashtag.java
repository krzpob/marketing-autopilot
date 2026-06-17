package pl.autopilot.datacollector.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class MonitoredHashtag {

    @Default
    private final UUID id = UUID.randomUUID();

    private final String ownerIgId;
    private final String hashtag;

    @Default
    private final boolean active = true;

    @Default
    private final Instant createdAt = Instant.now();

    private final Instant lastCollectedAt;
}