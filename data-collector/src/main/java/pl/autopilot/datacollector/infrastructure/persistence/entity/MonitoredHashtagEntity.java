package pl.autopilot.datacollector.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitored_hashtags")
@Getter
@Setter
@NoArgsConstructor
public class MonitoredHashtagEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "owner_ig_id", nullable = false)
    private String ownerIgId;

    @Column(nullable = false)
    private String hashtag;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_collected_at")
    private Instant lastCollectedAt;
}