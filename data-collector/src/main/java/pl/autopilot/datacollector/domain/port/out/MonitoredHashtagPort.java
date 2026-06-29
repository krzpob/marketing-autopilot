package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.MonitoredHashtag;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredHashtagPort {

    void save(MonitoredHashtag hashtag);

    List<MonitoredHashtag> findAllActive();

    List<MonitoredHashtag> findAllActiveByHashtag(String hashtag);

    Optional<MonitoredHashtag> findByOwnerIgIdAndHashtag(String ownerIgId, String hashtag);

    void updateLastCollectedAt(UUID id, Instant lastCollectedAt);

    void delete(UUID id);
}