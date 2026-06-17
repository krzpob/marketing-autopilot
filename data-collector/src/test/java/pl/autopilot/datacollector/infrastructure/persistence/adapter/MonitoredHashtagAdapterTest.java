package pl.autopilot.datacollector.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredHashtagEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.MonitoredHashtagJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MonitoredHashtagAdapterTest {

    @Mock
    private MonitoredHashtagJpaRepository repository;

    @InjectMocks
    private MonitoredHashtagAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        // given
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        adapter.save(aHashtag());

        // then
        BDDMockito.then(repository).should().save(any(MonitoredHashtagEntity.class));
    }

    // ── findAllActive ────────────────────────────────────────────────────────

    @Test
    void shouldReturnOnlyActiveHashtags() {
        // given
        given(repository.findAllByActiveTrue())
                .willReturn(List.of(anEntity(), anEntity()));

        // when
        List<MonitoredHashtag> result = adapter.findAllActive();

        // then
        then(result).hasSize(2);
        then(result).allMatch(MonitoredHashtag::isActive);
        BDDMockito.then(repository).should().findAllByActiveTrue();
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveHashtags() {
        // given
        given(repository.findAllByActiveTrue()).willReturn(List.of());

        // when / then
        then(adapter.findAllActive()).isEmpty();
    }

    // ── findAllActiveByHashtag ────────────────────────────────────────────────

    @Test
    void shouldReturnActiveHashtagsForGivenHashtag() {
        // given
        given(repository.findAllByHashtagAndActiveTrue("fotografia"))
                .willReturn(List.of(anEntity(), anEntity()));

        // when
        List<MonitoredHashtag> result = adapter.findAllActiveByHashtag("fotografia");

        // then
        then(result).hasSize(2);
        then(result).allMatch(h -> h.getHashtag().equals("fotografia"));
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveHashtagsForGivenHashtag() {
        // given
        given(repository.findAllByHashtagAndActiveTrue("nieznany")).willReturn(List.of());

        // when / then
        then(adapter.findAllActiveByHashtag("nieznany")).isEmpty();
    }

    // ── findByOwnerIgIdAndHashtag ─────────────────────────────────────────────

    @Test
    void shouldReturnHashtagWhenFound() {
        // given
        given(repository.findByOwnerIgIdAndHashtag("ig_123", "fotografia"))
                .willReturn(Optional.of(anEntity()));

        // when
        Optional<MonitoredHashtag> result =
                adapter.findByOwnerIgIdAndHashtag("ig_123", "fotografia");

        // then
        then(result).isPresent();
        then(result.get().getHashtag()).isEqualTo("fotografia");
    }

    @Test
    void shouldReturnEmptyWhenHashtagNotFound() {
        // given
        given(repository.findByOwnerIgIdAndHashtag(any(), any()))
                .willReturn(Optional.empty());

        // when / then
        then(adapter.findByOwnerIgIdAndHashtag("ig_123", "nieznany")).isEmpty();
    }

    // ── updateLastCollectedAt ─────────────────────────────────────────────────

    @Test
    void shouldUpdateLastCollectedAtWhenEntityExists() {
        // given
        UUID    id      = UUID.randomUUID();
        Instant newTime = Instant.now();

        given(repository.findById(id)).willReturn(Optional.of(anEntity(id)));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        adapter.updateLastCollectedAt(id, newTime);

        // then
        BDDMockito.then(repository).should().save(
                BDDMockito.argThat(e -> newTime.equals(e.getLastCollectedAt())));
    }

    @Test
    void shouldDoNothingWhenEntityNotFoundForUpdate() {
        // given
        UUID id = UUID.randomUUID();
        given(repository.findById(id)).willReturn(Optional.empty());

        // when
        adapter.updateLastCollectedAt(id, Instant.now());

        // then
        BDDMockito.then(repository).should(BDDMockito.never()).save(any());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void shouldDelegateDeleteToRepository() {
        // given
        UUID id = UUID.randomUUID();

        // when
        adapter.delete(id);

        // then
        BDDMockito.then(repository).should().deleteById(id);
    }

    // ── round-trip mapping ────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        // given
        UUID    id      = UUID.randomUUID();
        Instant created = Instant.now().minusSeconds(3600);
        Instant lastCol = Instant.now().minusSeconds(600);

        MonitoredHashtagEntity entity = new MonitoredHashtagEntity();
        entity.setId(id);
        entity.setOwnerIgId("ig_123");
        entity.setHashtag("fotografia");
        entity.setActive(true);
        entity.setCreatedAt(created);
        entity.setLastCollectedAt(lastCol);

        given(repository.findByOwnerIgIdAndHashtag("ig_123", "fotografia"))
                .willReturn(Optional.of(entity));

        // when
        MonitoredHashtag result = adapter
                .findByOwnerIgIdAndHashtag("ig_123", "fotografia")
                .orElseThrow();

        // then
        then(result.getId()).isEqualTo(id);
        then(result.getOwnerIgId()).isEqualTo("ig_123");
        then(result.getHashtag()).isEqualTo("fotografia");
        then(result.isActive()).isTrue();
        then(result.getCreatedAt()).isEqualTo(created);
        then(result.getLastCollectedAt()).isEqualTo(lastCol);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredHashtag aHashtag() {
        return MonitoredHashtag.builder()
                .ownerIgId("ig_123")
                .hashtag("fotografia")
                .build();
    }

    private MonitoredHashtagEntity anEntity() {
        return anEntity(UUID.randomUUID());
    }

    private MonitoredHashtagEntity anEntity(UUID id) {
        MonitoredHashtagEntity entity = new MonitoredHashtagEntity();
        entity.setId(id);
        entity.setOwnerIgId("ig_123");
        entity.setHashtag("fotografia");
        entity.setActive(true);
        entity.setCreatedAt(Instant.now().minusSeconds(3600));
        return entity;
    }
}