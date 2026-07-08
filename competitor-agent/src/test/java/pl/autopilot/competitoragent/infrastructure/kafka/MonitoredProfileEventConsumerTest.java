package pl.autopilot.competitoragent.infrastructure.kafka;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.common.event.ChangeType;
import pl.autopilot.common.event.MonitoredProfileEvent;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredProfileProjectionEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredProfileProjectionJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class MonitoredProfileEventConsumerTest {

    @Mock
    private MonitoredProfileProjectionJpaRepository repository;

    @InjectMocks
    private MonitoredProfileEventConsumer consumer;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String OWNER_IG_ID = "owner_123";
    private static final String HANDLE      = "rywal_pl";

    // ── nowy rekord ───────────────────────────────────────────────────────────

    @Test
    void shouldCreateNewProjectionWhenNotExists() {
        // given
        given(repository.findByOwnerIgIdAndCompetitorIgHandle(OWNER_IG_ID, HANDLE))
                .willReturn(Optional.empty());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredProfileEvent event = anEvent(ChangeType.ADDED, true);

        // when
        consumer.monitoredProfile().accept(event);

        // then
        ArgumentCaptor<MonitoredProfileProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredProfileProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());

        MonitoredProfileProjectionEntity saved = captor.getValue();
        softly.then(saved.getId()).isNotNull();
        softly.then(saved.getOwnerIgId()).isEqualTo(OWNER_IG_ID);
        softly.then(saved.getCompetitorIgHandle()).isEqualTo(HANDLE);
        softly.then(saved.isActive()).isTrue();
        softly.then(saved.getUpdatedAt()).isNotNull();
    }

    // ── aktualizacja istniejącego ────────────────────────────────────────────

    @Test
    void shouldUpdateExistingProjectionPreservingId() {
        // given
        UUID existingId = UUID.randomUUID();
        MonitoredProfileProjectionEntity existing = new MonitoredProfileProjectionEntity();
        existing.setId(existingId);
        existing.setOwnerIgId(OWNER_IG_ID);
        existing.setCompetitorIgHandle(HANDLE);
        existing.setActive(true);
        existing.setUpdatedAt(Instant.now().minusSeconds(3600));

        given(repository.findByOwnerIgIdAndCompetitorIgHandle(OWNER_IG_ID, HANDLE))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredProfileEvent event = anEvent(ChangeType.REMOVED, false);

        // when
        consumer.monitoredProfile().accept(event);

        // then
        ArgumentCaptor<MonitoredProfileProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredProfileProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());

        MonitoredProfileProjectionEntity saved = captor.getValue();
        softly.then(saved.getId()).isEqualTo(existingId);
        softly.then(saved.isActive()).isFalse();
    }

    @Test
    void shouldDeactivateProjectionOnRemovedEvent() {
        // given
        MonitoredProfileProjectionEntity existing = new MonitoredProfileProjectionEntity();
        existing.setId(UUID.randomUUID());
        existing.setOwnerIgId(OWNER_IG_ID);
        existing.setCompetitorIgHandle(HANDLE);
        existing.setActive(true);

        given(repository.findByOwnerIgIdAndCompetitorIgHandle(OWNER_IG_ID, HANDLE))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredProfileEvent event = anEvent(ChangeType.REMOVED, false);

        // when
        consumer.monitoredProfile().accept(event);

        // then
        ArgumentCaptor<MonitoredProfileProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredProfileProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());
        softly.then(captor.getValue().isActive()).isFalse();
    }

    @Test
    void shouldReactivateProjectionOnAddedEventAfterRemoval() {
        // given
        MonitoredProfileProjectionEntity existing = new MonitoredProfileProjectionEntity();
        existing.setId(UUID.randomUUID());
        existing.setOwnerIgId(OWNER_IG_ID);
        existing.setCompetitorIgHandle(HANDLE);
        existing.setActive(false);

        given(repository.findByOwnerIgIdAndCompetitorIgHandle(OWNER_IG_ID, HANDLE))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredProfileEvent event = anEvent(ChangeType.ADDED, true);

        // when
        consumer.monitoredProfile().accept(event);

        // then
        ArgumentCaptor<MonitoredProfileProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredProfileProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());
        softly.then(captor.getValue().isActive()).isTrue();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredProfileEvent anEvent(ChangeType changeType, boolean active) {
        return MonitoredProfileEvent.newBuilder()
                .setChangeType(changeType)
                .setOwnerIgId(OWNER_IG_ID)
                .setCompetitorIgHandle(HANDLE)
                .setActive(active)
                .setOccurredAt(Instant.now())
                .build();
    }
}