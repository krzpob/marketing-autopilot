package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "engagement_analyses")
@Getter
@Setter
@NoArgsConstructor
public class EngagementAnalysisEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "ig_media_id", nullable = false, unique = true)
    private String igMediaId;

    @Column(name = "competitor_username", nullable = false)
    private String competitorUsername;

    @Column(name = "engagement_rate", nullable = false)
    private double engagementRate;

    @Column(name = "delta_vs_rolling_avg", nullable = false)
    private double deltaVsRollingAvg;

    @Column(nullable = false)
    private String level;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;
}