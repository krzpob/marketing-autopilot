package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.HashtagPerformance;
import pl.autopilot.competitoragent.domain.port.out.HashtagPerformancePort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.HashtagPerformanceEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.HashtagPerformanceJpaRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HashtagPerformanceAdapter implements HashtagPerformancePort {

    private final HashtagPerformanceJpaRepository repository;

    @Override
    public void save(HashtagPerformance performance) {
        repository.save(toEntity(performance));
    }

    @Override
    public Optional<HashtagPerformance> findLatestByHashtag(String hashtag) {
        return repository.findTopByHashtagOrderByCollectedAtDesc(hashtag)
                .map(this::toDomain);
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private HashtagPerformanceEntity toEntity(HashtagPerformance domain) {
        HashtagPerformanceEntity entity = new HashtagPerformanceEntity();
        entity.setId(domain.getId());
        entity.setHashtag(domain.getHashtag());
        entity.setIgHashtagId(domain.getIgHashtagId());
        entity.setTopMediaCount(domain.getTopMediaCount());
        entity.setAvgLikeCount(domain.getAvgLikeCount());
        entity.setAvgCommentsCount(domain.getAvgCommentsCount());
        entity.setTrend(domain.getTrend().name());
        entity.setTrendScore(domain.getTrendScore());
        entity.setCollectedAt(domain.getCollectedAt());
        entity.setPreviousCollectedAt(domain.getPreviousCollectedAt());
        return entity;
    }

    private HashtagPerformance toDomain(HashtagPerformanceEntity entity) {
        return HashtagPerformance.builder()
                .id(entity.getId())
                .hashtag(entity.getHashtag())
                .igHashtagId(entity.getIgHashtagId())
                .topMediaCount(entity.getTopMediaCount())
                .avgLikeCount(entity.getAvgLikeCount())
                .avgCommentsCount(entity.getAvgCommentsCount())
                .trend(HashtagPerformance.TrendDirection.valueOf(entity.getTrend()))
                .trendScore(entity.getTrendScore())
                .collectedAt(entity.getCollectedAt())
                .previousCollectedAt(entity.getPreviousCollectedAt())
                .build();
    }
}