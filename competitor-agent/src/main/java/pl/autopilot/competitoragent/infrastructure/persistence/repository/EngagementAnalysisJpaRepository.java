package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.EngagementAnalysisEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface EngagementAnalysisJpaRepository extends JpaRepository<EngagementAnalysisEntity, UUID> {
    Optional<EngagementAnalysisEntity> findByIgMediaId(String igMediaId);
    List<EngagementAnalysisEntity> findByCompetitorUsernameOrderByAnalyzedAtDesc(
            String competitorUsername);
}