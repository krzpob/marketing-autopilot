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
@Table(schema = "competitor", name = "hashtag_collected_posts")
@Getter
@Setter
@NoArgsConstructor
public class HashtagCollectedPostEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "ig_media_id", nullable = false)
    private String igMediaId;

    @Column(nullable = false)
    private String hashtag;

    @Column(name = "ig_hashtag_id", nullable = false)
    private String igHashtagId;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(columnDefinition = "TEXT")
    private String permalink;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 100)
    private List<String> hashtags;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}