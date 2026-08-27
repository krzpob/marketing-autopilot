package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.MonitoredHashtagEvent;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagEventPort;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
class MonitoredHashtagEventKafkaAdapter implements MonitoredHashtagEventPort {

    private final StreamBridge streamBridge;

    @Override
    public void publish(MonitoredHashtag hashtag, ChangeType changeType) {
        MonitoredHashtagEvent event = MonitoredHashtagEvent.newBuilder()
                .setChangeType(pl.autopilot.common.event.ChangeType.valueOf(changeType.name()))
                .setOwnerIgId(hashtag.getOwnerIgId())
                .setHashtag(hashtag.getHashtag())
                .setActive(hashtag.isActive())
                .setOccurredAt(Instant.now())
                .setPlatform(hashtag.getPlatform().name())
                .build();

        boolean sent = streamBridge.send("monitored-hashtag-out-0", event);
        if (sent) {
            log.debug("Event MonitoredHashtag opublikowany: {} → #{} ({})",
                    hashtag.getOwnerIgId(), hashtag.getHashtag(), changeType);
        } else {
            log.error("Błąd publikacji eventu MonitoredHashtag dla {} → #{}",
                    hashtag.getOwnerIgId(), hashtag.getHashtag());
        }
    }
}