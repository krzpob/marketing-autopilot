package pl.autopilot.competitoragent.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "competitor", name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisResultEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "trigger_event_id", nullable = false)
    private String triggerEventId;

    @Column(name = "competitor_username")
    private String competitorUsername;

    @Column(name = "analysis_type", nullable = false)
    private String analysisType;

    @Column(name = "top_hashtags", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 50)
    private List<String> topHashtags;

    @Column(name = "optimal_posting_hour")
    private String optimalPostingHour;

    @Column(nullable = false)
    private String status;

    @Column(name = "engagement_analysis_id")
    private UUID engagementAnalysisId;

    @Column(name = "hashtag_performance_id")
    private UUID hashtagPerformanceId;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;
}