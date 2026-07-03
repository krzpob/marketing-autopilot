package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.domain.port.out.CompetitorPostPort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.CompetitorPostEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.CompetitorPostJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompetitorPostAdapter implements CompetitorPostPort {

    private final CompetitorPostJpaRepository repository;

    @Override
    public void save(CompetitorPost post) {
        repository.save(toEntity(post));
    }

    @Override
    public Optional<CompetitorPost> findByIgMediaId(String igMediaId) {
        return repository.findByIgMediaId(igMediaId).map(this::toDomain);
    }

    @Override
    public boolean existsByIgMediaId(String igMediaId) {
        return repository.existsByIgMediaId(igMediaId);
    }

    @Override
    public List<CompetitorPost> findLatestByUsername(String competitorUsername, int limit) {
        return repository
                .findTop30ByCompetitorUsernameOrderByPublishedAtDesc(competitorUsername)
                .stream()
                .limit(limit)
                .map(this::toDomain)
                .toList();
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private CompetitorPostEntity toEntity(CompetitorPost domain) {
        CompetitorPostEntity entity = new CompetitorPostEntity();
        entity.setId(domain.getId());
        entity.setIgMediaId(domain.getIgMediaId());
        entity.setShortcode(domain.getShortcode());
        entity.setCompetitorUsername(domain.getCompetitorUsername());
        entity.setOwnerIgId(domain.getOwnerIgId());
        entity.setMediaType(domain.getMediaType().name());
        entity.setCaption(domain.getCaption());
        entity.setHashtags(domain.getHashtags());
        entity.setMediaUrl(domain.getMediaUrl());
        entity.setLikeCount(domain.getLikeCount());
        entity.setCommentsCount(domain.getCommentsCount());
        entity.setFollowerCountAtCollection(domain.getFollowerCountAtCollection());
        entity.setPublishedAt(domain.getPublishedAt());
        entity.setCollectedAt(domain.getCollectedAt());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private CompetitorPost toDomain(CompetitorPostEntity entity) {
        return CompetitorPost.builder()
                .id(entity.getId())
                .igMediaId(entity.getIgMediaId())
                .shortcode(entity.getShortcode())
                .competitorUsername(entity.getCompetitorUsername())
                .ownerIgId(entity.getOwnerIgId())
                .mediaType(CompetitorPost.MediaType.valueOf(entity.getMediaType()))
                .caption(entity.getCaption())
                .hashtags(entity.getHashtags())
                .mediaUrl(entity.getMediaUrl())
                .likeCount(entity.getLikeCount())
                .commentsCount(entity.getCommentsCount())
                .followerCountAtCollection(entity.getFollowerCountAtCollection())
                .publishedAt(entity.getPublishedAt())
                .collectedAt(entity.getCollectedAt())
                .build();
    }
}
