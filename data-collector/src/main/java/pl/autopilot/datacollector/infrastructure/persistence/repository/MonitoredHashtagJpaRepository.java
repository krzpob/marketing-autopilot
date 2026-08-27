package pl.autopilot.datacollector.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredHashtagEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredHashtagJpaRepository extends JpaRepository<MonitoredHashtagEntity, UUID> {

    List<MonitoredHashtagEntity> findAllByOwnerIgId(String ownerIgId);

    List<MonitoredHashtagEntity> findAllByActiveTrue();

    List<MonitoredHashtagEntity> findAllByHashtagAndActiveTrue(String hashtag);

    Optional<MonitoredHashtagEntity> findByOwnerIgIdAndHashtag(String ownerIgId, String hashtag);

    Optional<MonitoredHashtagEntity> findByOwnerIgIdAndPlatformAndHashtag(
        String ownerIgId, String platform, String hashtag);

    List<MonitoredHashtagEntity> findAllByOwnerIgIdAndHashtag(String ownerIgId, String hashtag);
}