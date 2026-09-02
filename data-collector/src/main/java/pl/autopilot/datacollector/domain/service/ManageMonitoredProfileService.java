package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfileEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageMonitoredProfileService {

    private final MonitoredProfilePort      monitoredProfilePort;
    private final MonitoredProfileEventPort monitoredProfileEventPort;

    public MonitoredProfile addProfile(String ownerIgId, SocialMediaPlatform platform, String competitorHandle) {
        MonitoredProfile profile = monitoredProfilePort
                .findByOwnerIgIdAndPlatformAndHandle(ownerIgId, platform, competitorHandle)
                .map(existing -> existing.toBuilder().active(true).build())
                .orElseGet(() -> MonitoredProfile.builder()
                        .ownerIgId(ownerIgId)
                        .platform(platform)
                        .competitorIgHandle(competitorHandle)
                        .build());

        monitoredProfilePort.save(profile);
        log.info("Dodano/reaktywowano profil do obserwowania: {} → {}",
                ownerIgId, competitorHandle);

        monitoredProfileEventPort.publish(profile, ChangeType.ADDED);
        return profile;
    }

    public void deactivateProfile(String ownerIgId, SocialMediaPlatform platform, String competitorHandle) {
        MonitoredProfile profile = monitoredProfilePort
                .findByOwnerIgIdAndPlatformAndHandle(ownerIgId, platform, competitorHandle)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nie znaleziono obserwacji: " + ownerIgId + " → " + competitorHandle));

        deactivateProfile(profile);
    }

    public void deactivateProfileOnAllPlatforms(String onwerId, String competitorHandle) {
        monitoredProfilePort.findAllByOwnerIgIdAndHandle(onwerId, competitorHandle)
                .forEach(profile -> {
                    deactivateProfile(profile);
                });
    }   

    private void deactivateProfile(MonitoredProfile profile) {
        MonitoredProfile deactivated = profile.toBuilder().active(false).build();
        monitoredProfilePort.save(deactivated);
        log.info("Dezaktywowano profil: {} → {}", profile.getOwnerIgId(), profile.getCompetitorIgHandle());

        monitoredProfileEventPort.publish(deactivated, ChangeType.REMOVED);
    }
}