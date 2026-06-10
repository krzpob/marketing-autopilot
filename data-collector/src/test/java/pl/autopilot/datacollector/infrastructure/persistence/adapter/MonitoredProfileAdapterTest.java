package pl.autopilot.datacollector.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredProfileEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.MonitoredProfileJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MonitoredProfileAdapterTest {

    @Mock
    private MonitoredProfileJpaRepository repository;

    @InjectMocks
    private MonitoredProfileAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        // given
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        adapter.save(aProfile());

        // then
        BDDMockito.then(repository).should().save(any(MonitoredProfileEntity.class));
    }

    // ── findAllByOwnerIgId ───────────────────────────────────────────────────

    @Test
    void shouldReturnMappedProfilesForOwner() {
        // given
        given(repository.findAllByOwnerIgId("ig_123"))
                .willReturn(List.of(anEntity(), anEntity()));

        // when
        List<MonitoredProfile> result = adapter.findAllByOwnerIgId("ig_123");

        // then
        then(result).hasSize(2);
        then(result).allMatch(p -> p.getOwnerIgId().equals("ig_123"));
    }

    @Test
    void shouldReturnEmptyListWhenNoProfilesForOwner() {
        // given
        given(repository.findAllByOwnerIgId("unknown")).willReturn(List.of());

        // when / then
        then(adapter.findAllByOwnerIgId("unknown")).isEmpty();
    }

    // ── findAllActive ────────────────────────────────────────────────────────

    @Test
    void shouldReturnOnlyActiveProfiles() {
        // given
        given(repository.findAllByActiveTrue())
                .willReturn(List.of(anEntity()));

        // when
        List<MonitoredProfile> result = adapter.findAllActive();

        // then
        then(result).hasSize(1);
        then(result.get(0).isActive()).isTrue();
        BDDMockito.then(repository).should().findAllByActiveTrue();
    }

    // ── findByOwnerIgIdAndHandle ─────────────────────────────────────────────

    @Test
    void shouldReturnProfileWhenFound() {
        // given
        given(repository.findByOwnerIgIdAndCompetitorIgHandle("ig_123", "rywal_pl"))
                .willReturn(Optional.of(anEntity()));

        // when
        Optional<MonitoredProfile> result =
                adapter.findByOwnerIgIdAndHandle("ig_123", "rywal_pl");

        // then
        then(result).isPresent();
        then(result.get().getCompetitorIgHandle()).isEqualTo("rywal_pl");
    }

    @Test
    void shouldReturnEmptyWhenProfileNotFound() {
        // given
        given(repository.findByOwnerIgIdAndCompetitorIgHandle(any(), any()))
                .willReturn(Optional.empty());

        // when / then
        then(adapter.findByOwnerIgIdAndHandle("ig_123", "nieznany")).isEmpty();
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void shouldDelegateDeleteToRepository() {
        // given
        UUID id = UUID.randomUUID();

        // when
        adapter.delete(id);

        // then
        BDDMockito.then(repository).should().deleteById(id);
    }

    // ── round-trip mapping ───────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        // given
        UUID id        = UUID.randomUUID();
        Instant created = Instant.now().minusSeconds(3600);
        Instant lastCol = Instant.now().minusSeconds(600);

        MonitoredProfileEntity entity = new MonitoredProfileEntity();
        entity.setId(id);
        entity.setOwnerIgId("ig_123");
        entity.setCompetitorIgHandle("rywal_pl");
        entity.setActive(true);
        entity.setCreatedAt(created);
        entity.setLastCollectedAt(lastCol);

        given(repository.findByOwnerIgIdAndCompetitorIgHandle("ig_123", "rywal_pl"))
                .willReturn(Optional.of(entity));

        // when
        MonitoredProfile result = adapter
                .findByOwnerIgIdAndHandle("ig_123", "rywal_pl")
                .orElseThrow();

        // then
        then(result.getId()).isEqualTo(id);
        then(result.getOwnerIgId()).isEqualTo("ig_123");
        then(result.getCompetitorIgHandle()).isEqualTo("rywal_pl");
        then(result.isActive()).isTrue();
        then(result.getCreatedAt()).isEqualTo(created);
        then(result.getLastCollectedAt()).isEqualTo(lastCol);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private MonitoredProfile aProfile() {
        return MonitoredProfile.builder()
                .ownerIgId("ig_123")
                .competitorIgHandle("rywal_pl")
                .build();
    }

    private MonitoredProfileEntity anEntity() {
        MonitoredProfileEntity entity = new MonitoredProfileEntity();
        entity.setId(UUID.randomUUID());
        entity.setOwnerIgId("ig_123");
        entity.setCompetitorIgHandle("rywal_pl");
        entity.setActive(true);
        entity.setCreatedAt(Instant.now().minusSeconds(3600));
        return entity;
    }
}
