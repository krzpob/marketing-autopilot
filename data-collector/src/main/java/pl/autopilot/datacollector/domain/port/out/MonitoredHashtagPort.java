package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredHashtagPort {

    void save(MonitoredHashtag hashtag);

    List<MonitoredHashtag> findAllByOwnerIgId(String ownerIgId);

    List<MonitoredHashtag> findAllActive();

    List<MonitoredHashtag> findAllActiveByHashtag(String hashtag);

    Optional<MonitoredHashtag> findByOwnerIgIdAndPlatformAndHashtag(String ownerIgId, SocialMediaPlatform platform, String hashtag);

    List<MonitoredHashtag> findAllByOwnerIgIdAndHashtag(String ownerIgId, String hashtag);

    void updateLastCollectedAt(UUID id, Instant lastCollectedAt);

    void delete(UUID id);
}