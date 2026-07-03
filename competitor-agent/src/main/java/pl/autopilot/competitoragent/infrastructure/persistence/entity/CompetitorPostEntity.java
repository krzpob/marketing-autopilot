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
@Table(schema = "competitor", name = "competitor_posts")
@Getter
@Setter
@NoArgsConstructor
public class CompetitorPostEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "ig_media_id", nullable = false, unique = true)
    private String igMediaId;

    @Column(name = "shortcode")
    private String shortcode;

    @Column(name = "competitor_username", nullable = false)
    private String competitorUsername;

    @Column(name = "owner_ig_id", nullable = false)
    private String ownerIgId;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "hashtags", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 100)
    private List<String> hashtags;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount;

    @Column(name = "follower_count_at_collection", nullable = false)
    private long followerCountAtCollection;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}