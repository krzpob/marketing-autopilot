package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.HashtagCollectedPost;
import pl.autopilot.competitoragent.domain.port.out.HashtagCollectedPostPort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.HashtagCollectedPostEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.HashtagCollectedPostJpaRepository;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HashtagCollectedPostAdapter implements HashtagCollectedPostPort {

    private final HashtagCollectedPostJpaRepository repository;

    @Override
    public void save(HashtagCollectedPost post) {
        repository.save(toEntity(post));
    }

    @Override
    public boolean existsByIgMediaIdAndHashtag(String igMediaId, String hashtag) {
        return repository.existsByIgMediaIdAndHashtag(igMediaId, hashtag);
    }

    @Override
    public Optional<HashtagCollectedPost> findByIgMediaIdAndHashtag(
            String igMediaId, String hashtag) {
        return repository.findByIgMediaIdAndHashtag(igMediaId, hashtag)
                .map(this::toDomain);
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private HashtagCollectedPostEntity toEntity(HashtagCollectedPost domain) {
        HashtagCollectedPostEntity entity = new HashtagCollectedPostEntity();
        entity.setId(domain.getId());
        entity.setIgMediaId(domain.getIgMediaId());
        entity.setHashtag(domain.getHashtag());
        entity.setIgHashtagId(domain.getIgHashtagId());
        entity.setMediaType(domain.getMediaType().name());
        entity.setPermalink(domain.getPermalink());
        entity.setCaption(domain.getCaption());
        entity.setHashtags(domain.getHashtags());
        entity.setLikeCount(domain.getLikeCount());
        entity.setCommentsCount(domain.getCommentsCount());
        entity.setPublishedAt(domain.getPublishedAt());
        entity.setCollectedAt(domain.getCollectedAt());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private HashtagCollectedPost toDomain(HashtagCollectedPostEntity entity) {
        return HashtagCollectedPost.builder()
                .id(entity.getId())
                .igMediaId(entity.getIgMediaId())
                .hashtag(entity.getHashtag())
                .igHashtagId(entity.getIgHashtagId())
                .mediaType(HashtagCollectedPost.MediaType.valueOf(entity.getMediaType()))
                .permalink(entity.getPermalink())
                .caption(entity.getCaption())
                .hashtags(entity.getHashtags())
                .likeCount(entity.getLikeCount())
                .commentsCount(entity.getCommentsCount())
                .publishedAt(entity.getPublishedAt())
                .collectedAt(entity.getCollectedAt())
                .build();
    }
}