package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredHashtagProjectionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredHashtagProjectionJpaRepository
        extends JpaRepository<MonitoredHashtagProjectionEntity, UUID> {

    Optional<MonitoredHashtagProjectionEntity> findByOwnerIgIdAndHashtag(
            String ownerIgId, String hashtag);

    List<MonitoredHashtagProjectionEntity> findAllByHashtagAndActiveTrue(String hashtag);

    List<MonitoredHashtagProjectionEntity> findAllByOwnerIgIdAndActiveTrue(String ownerIgId);
}