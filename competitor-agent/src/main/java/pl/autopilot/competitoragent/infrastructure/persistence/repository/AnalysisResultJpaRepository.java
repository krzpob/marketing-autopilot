package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.AnalysisResultEntity;
import java.util.List;
import java.util.UUID;

interface AnalysisResultJpaRepository extends JpaRepository<AnalysisResultEntity, UUID> {
    List<AnalysisResultEntity> findByCompetitorUsernameOrderByAnalyzedAtDesc(
            String competitorUsername);
    boolean existsByTriggerEventId(String triggerEventId);
}
