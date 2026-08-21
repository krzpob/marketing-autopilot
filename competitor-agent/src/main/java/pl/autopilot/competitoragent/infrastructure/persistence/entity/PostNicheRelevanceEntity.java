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
@Table(schema = "competitor", name = "post_niche_relevance")
@Getter
@Setter
@NoArgsConstructor
public class PostNicheRelevanceEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "ig_media_id", nullable = false)
    private String igMediaId;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "owner_ig_id", nullable = false)
    private String ownerIgId;

    @Column(name = "matched_hashtags", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 100)
    private List<String> matchedHashtags;

    @Column(nullable = false)
    private double weight;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;
}