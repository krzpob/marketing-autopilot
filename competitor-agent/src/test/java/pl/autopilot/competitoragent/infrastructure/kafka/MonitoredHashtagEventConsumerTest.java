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
import pl.autopilot.common.event.MonitoredHashtagEvent;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredHashtagProjectionEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredHashtagProjectionJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class MonitoredHashtagEventConsumerTest {

    @Mock
    private MonitoredHashtagProjectionJpaRepository repository;

    @InjectMocks
    private MonitoredHashtagEventConsumer consumer;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String OWNER_IG_ID = "owner_123";
    private static final String HASHTAG     = "fotografia";

    // ── nowy rekord ───────────────────────────────────────────────────────────

    @Test
    void shouldCreateNewProjectionWhenNotExists() {
        // given
        given(repository.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.empty());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredHashtagEvent event = anEvent(ChangeType.ADDED, true);

        // when
        Consumer<MonitoredHashtagEvent> fn = consumer.monitoredHashtag();
        fn.accept(event);

        // then
        ArgumentCaptor<MonitoredHashtagProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredHashtagProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());

        MonitoredHashtagProjectionEntity saved = captor.getValue();
        softly.then(saved.getId()).isNotNull();
        softly.then(saved.getOwnerIgId()).isEqualTo(OWNER_IG_ID);
        softly.then(saved.getHashtag()).isEqualTo(HASHTAG);
        softly.then(saved.isActive()).isTrue();
        softly.then(saved.getUpdatedAt()).isNotNull();
    }

    // ── aktualizacja istniejącego ────────────────────────────────────────────

    @Test
    void shouldUpdateExistingProjectionPreservingId() {
        // given
        UUID existingId = UUID.randomUUID();
        MonitoredHashtagProjectionEntity existing = new MonitoredHashtagProjectionEntity();
        existing.setId(existingId);
        existing.setOwnerIgId(OWNER_IG_ID);
        existing.setHashtag(HASHTAG);
        existing.setActive(true);
        existing.setUpdatedAt(Instant.now().minusSeconds(3600));

        given(repository.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredHashtagEvent event = anEvent(ChangeType.REMOVED, false);

        // when
        consumer.monitoredHashtag().accept(event);

        // then
        ArgumentCaptor<MonitoredHashtagProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredHashtagProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());

        MonitoredHashtagProjectionEntity saved = captor.getValue();
        softly.then(saved.getId()).isEqualTo(existingId); // ten sam rekord, nie nowy
        softly.then(saved.isActive()).isFalse();
    }

    @Test
    void shouldDeactivateProjectionOnRemovedEvent() {
        // given
        MonitoredHashtagProjectionEntity existing = new MonitoredHashtagProjectionEntity();
        existing.setId(UUID.randomUUID());
        existing.setOwnerIgId(OWNER_IG_ID);
        existing.setHashtag(HASHTAG);
        existing.setActive(true);

        given(repository.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredHashtagEvent event = anEvent(ChangeType.REMOVED, false);

        // when
        consumer.monitoredHashtag().accept(event);

        // then
        ArgumentCaptor<MonitoredHashtagProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredHashtagProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());
        softly.then(captor.getValue().isActive()).isFalse();
    }

    @Test
    void shouldReactivateProjectionOnAddedEventAfterRemoval() {
        // given
        MonitoredHashtagProjectionEntity existing = new MonitoredHashtagProjectionEntity();
        existing.setId(UUID.randomUUID());
        existing.setOwnerIgId(OWNER_IG_ID);
        existing.setHashtag(HASHTAG);
        existing.setActive(false);

        given(repository.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MonitoredHashtagEvent event = anEvent(ChangeType.ADDED, true);

        // when
        consumer.monitoredHashtag().accept(event);

        // then
        ArgumentCaptor<MonitoredHashtagProjectionEntity> captor =
                ArgumentCaptor.forClass(MonitoredHashtagProjectionEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());
        softly.then(captor.getValue().isActive()).isTrue();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredHashtagEvent anEvent(ChangeType changeType, boolean active) {
        return MonitoredHashtagEvent.newBuilder()
                .setChangeType(changeType)
                .setOwnerIgId(OWNER_IG_ID)
                .setHashtag(HASHTAG)
                .setActive(active)
                .setOccurredAt(Instant.now())
                .build();
    }
}