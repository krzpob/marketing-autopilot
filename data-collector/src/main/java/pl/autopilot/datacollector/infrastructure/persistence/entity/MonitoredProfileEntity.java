package pl.autopilot.datacollector.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitored_profiles")
@Getter
@Setter
@NoArgsConstructor
public class MonitoredProfileEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "owner_ig_id", nullable = false)
    private String ownerIgId;

    @Column(name = "competitor_ig_handle", nullable = false)
    private String competitorIgHandle;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_collected_at")
    private Instant lastCollectedAt;
}