package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredProfileProjectionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredProfileProjectionJpaRepository
        extends JpaRepository<MonitoredProfileProjectionEntity, UUID> {

    Optional<MonitoredProfileProjectionEntity> findByOwnerIgIdAndCompetitorIgHandle(
            String ownerIgId, String competitorIgHandle);

    List<MonitoredProfileProjectionEntity> findAllByCompetitorIgHandleAndActiveTrue(
            String competitorIgHandle);
}