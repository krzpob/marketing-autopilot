package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageMonitoredHashtagService {

    private final MonitoredHashtagPort      monitoredHashtagPort;
    private final MonitoredHashtagEventPort monitoredHashtagEventPort;

    public MonitoredHashtag addHashtag(String ownerIgId, String hashtag) {
        MonitoredHashtag monitoredHashtag = monitoredHashtagPort
                .findByOwnerIgIdAndHashtag(ownerIgId, hashtag)
                .map(existing -> existing.toBuilder().active(true).build())
                .orElseGet(() -> MonitoredHashtag.builder()
                        .ownerIgId(ownerIgId)
                        .hashtag(hashtag)
                        .build());

        monitoredHashtagPort.save(monitoredHashtag);
        log.info("Dodano/reaktywowano hashtag do obserwowania: {} → #{}", ownerIgId, hashtag);

        monitoredHashtagEventPort.publish(monitoredHashtag, ChangeType.ADDED);
        return monitoredHashtag;
    }

    public void deactivateHashtag(String ownerIgId, String hashtag) {
        MonitoredHashtag monitoredHashtag = monitoredHashtagPort
                .findByOwnerIgIdAndHashtag(ownerIgId, hashtag)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nie znaleziono obserwacji: " + ownerIgId + " → #" + hashtag));

        MonitoredHashtag deactivated = monitoredHashtag.toBuilder().active(false).build();
        monitoredHashtagPort.save(deactivated);
        log.info("Dezaktywowano hashtag: {} → #{}", ownerIgId, hashtag);

        monitoredHashtagEventPort.publish(deactivated, ChangeType.REMOVED);
    }
}