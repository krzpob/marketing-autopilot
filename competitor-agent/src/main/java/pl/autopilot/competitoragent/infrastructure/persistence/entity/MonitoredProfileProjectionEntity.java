package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "monitored_profile_projection")
@Getter
@Setter
@NoArgsConstructor
public class MonitoredProfileProjectionEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "owner_ig_id", nullable = false)
    private String ownerIgId;

    @Column(name = "competitor_ig_handle", nullable = false)
    private String competitorIgHandle;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}