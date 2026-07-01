package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.CompetitorProfileEntity;
import java.util.Optional;
import java.util.UUID;

interface CompetitorProfileJpaRepository extends JpaRepository<CompetitorProfileEntity, UUID> {
    Optional<CompetitorProfileEntity> findByUsername(String username);
}
