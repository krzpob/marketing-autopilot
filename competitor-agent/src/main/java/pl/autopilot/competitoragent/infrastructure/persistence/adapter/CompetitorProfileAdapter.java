package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.CompetitorProfile;
import pl.autopilot.competitoragent.domain.port.out.CompetitorProfilePort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.CompetitorProfileEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.CompetitorProfileJpaRepository;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompetitorProfileAdapter implements CompetitorProfilePort {

    private final CompetitorProfileJpaRepository repository;

    @Override
    public void save(CompetitorProfile profile) {
        repository.save(toEntity(profile));
    }

    @Override
    public Optional<CompetitorProfile> findByUsername(String username) {
        return repository.findByUsername(username).map(this::toDomain);
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private CompetitorProfileEntity toEntity(CompetitorProfile domain) {
        CompetitorProfileEntity entity = new CompetitorProfileEntity();
        entity.setId(domain.getId());
        entity.setIgId(domain.getIgId());
        entity.setUsername(domain.getUsername());
        entity.setFollowerCount(domain.getFollowerCount());
        entity.setMediaCount(domain.getMediaCount());
        entity.setBiography(domain.getBiography());
        entity.setRollingAvgEngagementRate(domain.getRollingAvgEngagementRate());
        entity.setRollingWindowSize(domain.getRollingWindowSize());
        entity.setRollingAvgUpdatedAt(domain.getRollingAvgUpdatedAt());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private CompetitorProfile toDomain(CompetitorProfileEntity entity) {
        return CompetitorProfile.builder()
                .id(entity.getId())
                .igId(entity.getIgId())
                .username(entity.getUsername())
                .followerCount(entity.getFollowerCount())
                .mediaCount(entity.getMediaCount())
                .biography(entity.getBiography())
                .rollingAvgEngagementRate(entity.getRollingAvgEngagementRate())
                .rollingWindowSize(entity.getRollingWindowSize())
                .rollingAvgUpdatedAt(entity.getRollingAvgUpdatedAt())
                .build();
    }
}