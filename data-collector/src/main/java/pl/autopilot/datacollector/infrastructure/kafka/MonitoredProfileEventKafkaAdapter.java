package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.MonitoredProfileEvent;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfileEventPort;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
class MonitoredProfileEventKafkaAdapter implements MonitoredProfileEventPort {

    private final StreamBridge streamBridge;

    @Override
    public void publish(MonitoredProfile profile, ChangeType changeType) {
        MonitoredProfileEvent event = MonitoredProfileEvent.newBuilder()
                .setChangeType(pl.autopilot.common.event.ChangeType.valueOf(changeType.name()))
                .setOwnerIgId(profile.getOwnerIgId())
                .setCompetitorIgHandle(profile.getCompetitorIgHandle())
                .setActive(profile.isActive())
                .setOccurredAt(Instant.now())
                .setPlatform(profile.getPlatform().name())
                .build();

        boolean sent = streamBridge.send("monitored-profile-out-0", event);
        if (sent) {
            log.debug("Event MonitoredProfile opublikowany: {} → {} ({})",
                    profile.getOwnerIgId(), profile.getCompetitorIgHandle(), changeType);
        } else {
            log.error("Błąd publikacji eventu MonitoredProfile dla {} → {}",
                    profile.getOwnerIgId(), profile.getCompetitorIgHandle());
        }
    }
}