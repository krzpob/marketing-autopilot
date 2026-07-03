package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.EngagementAnalysis;
import pl.autopilot.competitoragent.domain.port.out.EngagementAnalysisPort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.EngagementAnalysisEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.EngagementAnalysisJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EngagementAnalysisAdapter implements EngagementAnalysisPort {

    private final EngagementAnalysisJpaRepository repository;

    @Override
    public void save(EngagementAnalysis analysis) {
        repository.save(toEntity(analysis));
    }

    @Override
    public Optional<EngagementAnalysis> findByIgMediaId(String igMediaId) {
        return repository.findByIgMediaId(igMediaId).map(this::toDomain);
    }

    @Override
    public List<EngagementAnalysis> findByUsername(String competitorUsername) {
        return repository
                .findByCompetitorUsernameOrderByAnalyzedAtDesc(competitorUsername)
                .stream().map(this::toDomain).toList();
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private EngagementAnalysisEntity toEntity(EngagementAnalysis domain) {
        EngagementAnalysisEntity entity = new EngagementAnalysisEntity();
        entity.setId(domain.getId());
        entity.setIgMediaId(domain.getIgMediaId());
        entity.setCompetitorUsername(domain.getCompetitorUsername());
        entity.setEngagementRate(domain.getEngagementRate());
        entity.setDeltaVsRollingAvg(domain.getDeltaVsRollingAvg());
        entity.setLevel(domain.getLevel().name());
        entity.setAnalyzedAt(domain.getAnalyzedAt());
        return entity;
    }

    private EngagementAnalysis toDomain(EngagementAnalysisEntity entity) {
        return EngagementAnalysis.builder()
                .id(entity.getId())
                .igMediaId(entity.getIgMediaId())
                .competitorUsername(entity.getCompetitorUsername())
                .engagementRate(entity.getEngagementRate())
                .deltaVsRollingAvg(entity.getDeltaVsRollingAvg())
                .level(EngagementAnalysis.EngagementLevel.valueOf(entity.getLevel()))
                .analyzedAt(entity.getAnalyzedAt())
                .build();
    }
}