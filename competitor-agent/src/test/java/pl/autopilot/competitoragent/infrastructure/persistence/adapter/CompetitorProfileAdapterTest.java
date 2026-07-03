package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.CompetitorProfile;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.CompetitorProfileEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.CompetitorProfileJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CompetitorProfileAdapterTest {

    @Mock
    private CompetitorProfileJpaRepository repository;

    @InjectMocks
    private CompetitorProfileAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        adapter.save(aProfile());

        BDDMockito.then(repository).should().save(any(CompetitorProfileEntity.class));
    }

    // ── findByUsername ────────────────────────────────────────────────────────

    @Test
    void shouldReturnMappedProfileWhenFound() {
        given(repository.findByUsername("fotografik_waw"))
                .willReturn(Optional.of(anEntity()));

        Optional<CompetitorProfile> result = adapter.findByUsername("fotografik_waw");

        then(result).isPresent();
        then(result.get().getUsername()).isEqualTo("fotografik_waw");
    }

    @Test
    void shouldReturnEmptyWhenProfileNotFound() {
        given(repository.findByUsername("unknown")).willReturn(Optional.empty());

        then(adapter.findByUsername("unknown")).isEmpty();
    }

    // ── round-trip mapping ────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        UUID    id        = UUID.randomUUID();
        Instant updatedAt = Instant.parse("2024-06-15T10:00:00Z");
        Instant rollingAt = Instant.parse("2024-06-14T10:00:00Z");

        CompetitorProfileEntity entity = new CompetitorProfileEntity();
        entity.setId(id);
        entity.setIgId("ig_123");
        entity.setUsername("fotografik_waw");
        entity.setFollowerCount(5000L);
        entity.setMediaCount(120);
        entity.setBiography("Fotograf z Warszawy");
        entity.setRollingAvgEngagementRate(3.5);
        entity.setRollingWindowSize(30);
        entity.setRollingAvgUpdatedAt(rollingAt);
        entity.setUpdatedAt(updatedAt);

        given(repository.findByUsername("fotografik_waw"))
                .willReturn(Optional.of(entity));

        CompetitorProfile result = adapter.findByUsername("fotografik_waw").orElseThrow();

        then(result.getId()).isEqualTo(id);
        then(result.getIgId()).isEqualTo("ig_123");
        then(result.getUsername()).isEqualTo("fotografik_waw");
        then(result.getFollowerCount()).isEqualTo(5000L);
        then(result.getMediaCount()).isEqualTo(120);
        then(result.getBiography()).isEqualTo("Fotograf z Warszawy");
        then(result.getRollingAvgEngagementRate()).isEqualTo(3.5);
        then(result.getRollingWindowSize()).isEqualTo(30);
        then(result.getRollingAvgUpdatedAt()).isEqualTo(rollingAt);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CompetitorProfile aProfile() {
        return CompetitorProfile.builder()
                .username("fotografik_waw")
                .followerCount(5000L)
                .mediaCount(120)
                .rollingWindowSize(30)
                .build();
    }

    private CompetitorProfileEntity anEntity() {
        CompetitorProfileEntity entity = new CompetitorProfileEntity();
        entity.setId(UUID.randomUUID());
        entity.setIgId("ig_123");
        entity.setUsername("fotografik_waw");
        entity.setFollowerCount(5000L);
        entity.setMediaCount(120);
        entity.setRollingAvgEngagementRate(3.5);
        entity.setRollingWindowSize(30);
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}