package pl.autopilot.competitoragent.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.MonitoredHashtagEvent;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredHashtagProjectionEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredHashtagProjectionJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
class MonitoredHashtagEventConsumer {

    private final MonitoredHashtagProjectionJpaRepository repository;

    @Bean
    Consumer<MonitoredHashtagEvent> monitoredHashtag() {
        return event -> {
            log.info("Odebrano MonitoredHashtagEvent: owner={} hashtag=#{} changeType={} active={}",
                    event.getOwnerIgId(), event.getHashtag(),
                    event.getChangeType(), event.getActive());

            Optional<MonitoredHashtagProjectionEntity> existing =
                    repository.findByOwnerIgIdAndHashtag(
                            event.getOwnerIgId(), event.getHashtag());

            MonitoredHashtagProjectionEntity entity = existing.orElseGet(() -> {
                MonitoredHashtagProjectionEntity e = new MonitoredHashtagProjectionEntity();
                e.setId(UUID.randomUUID());
                e.setOwnerIgId(event.getOwnerIgId());
                e.setHashtag(event.getHashtag());
                return e;
            });

            entity.setActive(event.getActive());
            entity.setUpdatedAt(Instant.now());
            repository.save(entity);

            log.debug("Zaktualizowano projekcję hashtagu: {} → #{} active={}",
                    event.getOwnerIgId(), event.getHashtag(), event.getActive());
        };
    }
}
