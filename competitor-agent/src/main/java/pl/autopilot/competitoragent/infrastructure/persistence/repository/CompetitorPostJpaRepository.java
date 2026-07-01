package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.CompetitorPostEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CompetitorPostJpaRepository extends JpaRepository<CompetitorPostEntity, UUID> {
    Optional<CompetitorPostEntity> findByIgMediaId(String igMediaId);
    List<CompetitorPostEntity> findTop30ByCompetitorUsernameOrderByPublishedAtDesc(
            String competitorUsername);
    boolean existsByIgMediaId(String igMediaId);
}