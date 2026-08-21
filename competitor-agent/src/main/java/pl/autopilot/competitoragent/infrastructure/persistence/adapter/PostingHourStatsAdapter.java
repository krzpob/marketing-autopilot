package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.PostingHourStats;
import pl.autopilot.competitoragent.domain.port.out.PostingHourStatsPort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.PostingHourStatsEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.PostingHourStatsJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostingHourStatsAdapter implements PostingHourStatsPort {

    private final PostingHourStatsJpaRepository repository;

    @Override
    public void save(PostingHourStats stats) {
        repository.save(toEntity(stats));
    }

    @Override
    public Optional<PostingHourStats> findByUsernameAndMediaTypeAndHour(
            String competitorUsername, String mediaType, short hourOfDay) {
        return repository.findByCompetitorUsernameAndMediaTypeAndHourOfDay(
                competitorUsername, mediaType, hourOfDay).map(this::toDomain);
    }

    @Override
    public List<PostingHourStats> findByUsername(String competitorUsername) {
        return repository.findByCompetitorUsername(competitorUsername)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<PostingHourStats> findByUsernameAndMediaType(
            String competitorUsername, String mediaType) {
        return repository.findByCompetitorUsernameAndMediaType(
                competitorUsername, mediaType)
                .stream().map(this::toDomain).toList();
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private PostingHourStatsEntity toEntity(PostingHourStats domain) {
        PostingHourStatsEntity entity = new PostingHourStatsEntity();
        entity.setId(domain.getId());
        entity.setCompetitorUsername(domain.getCompetitorUsername());
        entity.setMediaType(domain.getMediaType());
        entity.setHourOfDay(domain.getHourOfDay());
        entity.setAvgEngagementRate(domain.getAvgEngagementRate());
        entity.setPostCount(domain.getPostCount());
        entity.setTotalLikeCount(domain.getTotalLikeCount());
        entity.setTotalCommentsCount(domain.getTotalCommentsCount());
        entity.setLastUpdatedAt(domain.getLastUpdatedAt());
        return entity;
    }

    private PostingHourStats toDomain(PostingHourStatsEntity entity) {
        return PostingHourStats.builder()
                .id(entity.getId())
                .competitorUsername(entity.getCompetitorUsername())
                .mediaType(entity.getMediaType())
                .hourOfDay(entity.getHourOfDay())
                .avgEngagementRate(entity.getAvgEngagementRate())
                .postCount(entity.getPostCount())
                .totalLikeCount(entity.getTotalLikeCount())
                .totalCommentsCount(entity.getTotalCommentsCount())
                .lastUpdatedAt(entity.getLastUpdatedAt())
                .build();
    }
}