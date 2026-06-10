package pl.autopilot.datacollector.domain.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Getter
@Builder(toBuilder = true)
public class MonitoredProfile {
    @Default
    private UUID id = UUID.randomUUID();
    private String ownerIgId;
    private String competitorIgHandle;

    @Default
    private boolean active = true;

    @Builder.Default
    private final Instant createdAt = Instant.now();

    private final Instant lastCollectedAt; 
}
