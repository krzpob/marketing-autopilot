package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.MonitoredProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredProfilePort {

    void save(MonitoredProfile profile);

    List<MonitoredProfile> findAllByOwnerIgId(String ownerIgId);

    /** Wszystkie aktywne profile — używane przez scheduler */
    List<MonitoredProfile> findAllActive();

    Optional<MonitoredProfile> findByOwnerIgIdAndHandle(String ownerIgId,
                                                         String competitorIgHandle);

    void delete(UUID id);
}