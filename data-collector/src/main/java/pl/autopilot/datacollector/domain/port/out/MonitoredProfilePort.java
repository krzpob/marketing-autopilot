package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.MonitoredProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface MonitoredProfilePort {

    void save(MonitoredProfile profile);

    List<MonitoredProfile> findAllByOwnerIgId(String ownerIgId);

    /** Wszystkie aktywne profile — używane przez scheduler */
    List<MonitoredProfile> findAllActive();

    /**
     * Znajdź profil obserwowania po ownerIgId i competitorHandle — do sprawdzania, czy dany profil jest już monitorowany
     * @param ownerIgId identyfikator właściciela (Instagram user ID)
     * @param competitorIgHandle handle konkurenta (np. "zieniuphoto")
     * @return  Optional z profilem obserwowania, jeśli istnieje, lub pusty Optional, jeśli nie istnieje
     */
    Optional<MonitoredProfile> findByOwnerIgIdAndHandle(String ownerIgId,
                                                         String competitorIgHandle);

    /** Wszystkie aktywne profile obserwujące dany handle — do rotacji tokenów *
     * @param competitorIgHandle handle konkurenta (np. "zieniuphoto")
     * @return  Lista aktywnych profili obserwowania, które monitorują danego konkurenta
    */
    List<MonitoredProfile> findAllActiveByCompetitorHandle(String competitorIgHandle);

    /** Aktualizacja znacznika ostatniego pobrania *
     * 
     * @param id identyfikator profilu obserwowania (UUID)
     * @param lastCollectedAt znacznik czasu ostatniego pobrania danych dla tego profilu (Instant)
    */
    void updateLastCollectedAt(UUID id, Instant lastCollectedAt);

    void delete(UUID id);
}