package pl.autopilot.datacollector.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredProfileEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredProfileJpaRepository
        extends JpaRepository<MonitoredProfileEntity, UUID> {

    List<MonitoredProfileEntity> findAllByOwnerIgId(String ownerIgId);

    List<MonitoredProfileEntity> findAllByActiveTrue();

    Optional<MonitoredProfileEntity> findByOwnerIgIdAndCompetitorIgHandle(
            String ownerIgId, String competitorIgHandle);
}