package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.AnalysisResult;
import pl.autopilot.competitoragent.domain.port.out.AnalysisResultPort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.AnalysisResultEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.AnalysisResultJpaRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisResultAdapter implements AnalysisResultPort {

    private final AnalysisResultJpaRepository repository;

    @Override
    public void save(AnalysisResult result) {
        repository.save(toEntity(result));
    }

    @Override
    public boolean existsByTriggerEventId(String triggerEventId) {
        return repository.existsByTriggerEventId(triggerEventId);
    }

    @Override
    public List<AnalysisResult> findByCompetitorUsername(String competitorUsername) {
        return repository
                .findByCompetitorUsernameOrderByAnalyzedAtDesc(competitorUsername)
                .stream().map(this::toDomain).toList();
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private AnalysisResultEntity toEntity(AnalysisResult domain) {
        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.setId(domain.getId());
        entity.setTriggerEventId(domain.getTriggerEventId());
        entity.setCompetitorUsername(domain.getCompetitorUsername());
        entity.setAnalysisType(domain.getAnalysisType().name());
        entity.setTopHashtags(domain.getTopHashtags());
        entity.setOptimalPostingHour(domain.getOptimalPostingHour());
        entity.setStatus(domain.getStatus().name());
        entity.setEngagementAnalysisId(
                domain.getEngagementAnalysis() != null
                        ? domain.getEngagementAnalysis().getId()
                        : null);
        entity.setHashtagPerformanceId(
                domain.getHashtagPerformance() != null
                        ? domain.getHashtagPerformance().getId()
                        : null);
        entity.setAnalyzedAt(domain.getAnalyzedAt());
        return entity;
    }

    private AnalysisResult toDomain(AnalysisResultEntity entity) {
        return AnalysisResult.builder()
                .id(entity.getId())
                .triggerEventId(entity.getTriggerEventId())
                .competitorUsername(entity.getCompetitorUsername())
                .analysisType(AnalysisResult.AnalysisType.valueOf(entity.getAnalysisType()))
                .topHashtags(entity.getTopHashtags())
                .optimalPostingHour(entity.getOptimalPostingHour())
                .status(AnalysisResult.AnalysisStatus.valueOf(entity.getStatus()))
                .analyzedAt(entity.getAnalyzedAt())
                .build();
    }
}