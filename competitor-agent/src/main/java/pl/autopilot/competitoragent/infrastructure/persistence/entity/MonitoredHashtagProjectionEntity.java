package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "monitored_hashtag_projection")
@Getter
@Setter
@NoArgsConstructor
public class MonitoredHashtagProjectionEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "owner_ig_id", nullable = false)
    private String ownerIgId;

    @Column(nullable = false)
    private String hashtag;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}