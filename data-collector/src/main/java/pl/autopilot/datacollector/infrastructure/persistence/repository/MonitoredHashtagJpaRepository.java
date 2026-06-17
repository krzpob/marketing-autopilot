package pl.autopilot.datacollector.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredHashtagEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredHashtagJpaRepository extends JpaRepository<MonitoredHashtagEntity, UUID> {

    List<MonitoredHashtagEntity> findAllByActiveTrue();

    List<MonitoredHashtagEntity> findAllByHashtagAndActiveTrue(String hashtag);

    Optional<MonitoredHashtagEntity> findByOwnerIgIdAndHashtag(String ownerIgId, String hashtag);
}