package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "posting_hour_stats")
@Getter
@Setter
@NoArgsConstructor
public class PostingHourStatsEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "competitor_username", nullable = false)
    private String competitorUsername;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "hour_of_day", nullable = false)
    private short hourOfDay;

    @Column(name = "avg_engagement_rate", nullable = false)
    private double avgEngagementRate;

    @Column(name = "post_count", nullable = false)
    private int postCount;

    @Column(name = "total_like_count", nullable = false)
    private long totalLikeCount;

    @Column(name = "total_comments_count", nullable = false)
    private long totalCommentsCount;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}