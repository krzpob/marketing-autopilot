package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "competitor_profiles")
@Getter
@Setter
@NoArgsConstructor
public class CompetitorProfileEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "ig_id")
    private String igId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "follower_count", nullable = false)
    private long followerCount;

    @Column(name = "media_count", nullable = false)
    private int mediaCount;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "rolling_avg_engagement_rate", nullable = false)
    private double rollingAvgEngagementRate;

    @Column(name = "rolling_window_size", nullable = false)
    private int rollingWindowSize;

    @Column(name = "rolling_avg_updated_at")
    private Instant rollingAvgUpdatedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}