package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageMonitoredHashtagService {

    private final MonitoredHashtagPort      monitoredHashtagPort;
    private final MonitoredHashtagEventPort monitoredHashtagEventPort;

    public MonitoredHashtag addHashtag(String ownerIgId, SocialMediaPlatform platform, String hashtag) {
        MonitoredHashtag monitoredHashtag = monitoredHashtagPort
                .findByOwnerIgIdAndPlatformAndHashtag(ownerIgId, platform, hashtag)
                .map(existing -> existing.toBuilder().active(true).build())
                .orElseGet(() -> MonitoredHashtag.builder()
                        .ownerIgId(ownerIgId)
                        .platform(platform)
                        .hashtag(hashtag)
                        .build());

        monitoredHashtagPort.save(monitoredHashtag);
        log.info("Dodano/reaktywowano hashtag do obserwowania: {} → #{}", ownerIgId, hashtag);

        monitoredHashtagEventPort.publish(monitoredHashtag, ChangeType.ADDED);
        return monitoredHashtag;
    }

    public void deactivateHashtag(String ownerIgId, SocialMediaPlatform platform, String hashtag) {
        MonitoredHashtag monitoredHashtag = monitoredHashtagPort
                .findByOwnerIgIdAndPlatformAndHashtag(ownerIgId, platform, hashtag)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nie znaleziono obserwacji: " + ownerIgId + " → #" + hashtag));

        deactivateHashtag(monitoredHashtag);
    }

    public void deactivateHashtagOnAllPlatforms(String ownerIgId, String hashtag) {
        monitoredHashtagPort.findAllByOwnerIgIdAndHashtag(ownerIgId, hashtag)
                .forEach(monitoredHashtag -> {
                    deactivateHashtag(monitoredHashtag);
                });
    }

    private void deactivateHashtag(MonitoredHashtag monitoredHashtag) {
        MonitoredHashtag deactivated = monitoredHashtag.toBuilder().active(false).build();
        monitoredHashtagPort.save(deactivated);
        log.info("Dezaktywowano hashtag: {} → #{}", monitoredHashtag.getOwnerIgId(), monitoredHashtag.getHashtag());

        monitoredHashtagEventPort.publish(deactivated, ChangeType.REMOVED);
    }

}