package pl.autopilot.competitoragent.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.MonitoredProfileEvent;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredProfileProjectionEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredProfileProjectionJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
class MonitoredProfileEventConsumer {

    private final MonitoredProfileProjectionJpaRepository repository;

    @Bean
    Consumer<MonitoredProfileEvent> monitoredProfile() {
        return event -> {
            log.info("Odebrano MonitoredProfileEvent: owner={} handle={} changeType={} active={}",
                    event.getOwnerIgId(), event.getCompetitorIgHandle(),
                    event.getChangeType(), event.getActive());

            Optional<MonitoredProfileProjectionEntity> existing =
                    repository.findByOwnerIgIdAndCompetitorIgHandle(
                            event.getOwnerIgId(), event.getCompetitorIgHandle());

            MonitoredProfileProjectionEntity entity = existing.orElseGet(() -> {
                MonitoredProfileProjectionEntity e = new MonitoredProfileProjectionEntity();
                e.setId(UUID.randomUUID());
                e.setOwnerIgId(event.getOwnerIgId());
                e.setCompetitorIgHandle(event.getCompetitorIgHandle());
                return e;
            });

            entity.setActive(event.getActive());
            entity.setUpdatedAt(Instant.now());
            repository.save(entity);

            log.debug("Zaktualizowano projekcję profilu: {} → {} active={}",
                    event.getOwnerIgId(), event.getCompetitorIgHandle(), event.getActive());
        };
    }
}