package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "hashtag_performances")
@Getter
@Setter
@NoArgsConstructor
public class HashtagPerformanceEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String hashtag;

    @Column(name = "ig_hashtag_id", nullable = false)
    private String igHashtagId;

    @Column(name = "top_media_count", nullable = false)
    private int topMediaCount;

    @Column(name = "avg_like_count", nullable = false)
    private long avgLikeCount;

    @Column(name = "avg_comments_count", nullable = false)
    private int avgCommentsCount;

    @Column(nullable = false)
    private String trend;

    @Column(name = "trend_score", nullable = false)
    private double trendScore;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "previous_collected_at")
    private Instant previousCollectedAt;
}