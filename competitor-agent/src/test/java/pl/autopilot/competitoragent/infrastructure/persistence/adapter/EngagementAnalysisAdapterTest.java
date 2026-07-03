package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.EngagementAnalysis;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.EngagementAnalysisEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.EngagementAnalysisJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EngagementAnalysisAdapterTest {

    @Mock
    private EngagementAnalysisJpaRepository repository;

    @InjectMocks
    private EngagementAnalysisAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        adapter.save(anAnalysis());

        BDDMockito.then(repository).should().save(any(EngagementAnalysisEntity.class));
    }

    // ── findByIgMediaId ───────────────────────────────────────────────────────

    @Test
    void shouldReturnMappedAnalysisWhenFound() {
        given(repository.findByIgMediaId("media123"))
                .willReturn(Optional.of(anEntity()));

        Optional<EngagementAnalysis> result = adapter.findByIgMediaId("media123");

        then(result).isPresent();
        then(result.get().getIgMediaId()).isEqualTo("media123");
    }

    @Test
    void shouldReturnEmptyWhenAnalysisNotFound() {
        given(repository.findByIgMediaId("unknown")).willReturn(Optional.empty());

        then(adapter.findByIgMediaId("unknown")).isEmpty();
    }

    // ── findByUsername ────────────────────────────────────────────────────────

    @Test
    void shouldReturnAnalysesForUsername() {
        given(repository.findByCompetitorUsernameOrderByAnalyzedAtDesc("fotografik_waw"))
                .willReturn(List.of(anEntity(), anEntity()));

        List<EngagementAnalysis> result = adapter.findByUsername("fotografik_waw");

        then(result).hasSize(2);
    }

    // ── round-trip mapping ────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        UUID    id         = UUID.randomUUID();
        Instant analyzedAt = Instant.parse("2024-06-15T10:00:00Z");

        EngagementAnalysisEntity entity = new EngagementAnalysisEntity();
        entity.setId(id);
        entity.setIgMediaId("media123");
        entity.setCompetitorUsername("fotografik_waw");
        entity.setEngagementRate(4.5);
        entity.setDeltaVsRollingAvg(1.2);
        entity.setLevel("HIGH");
        entity.setAnalyzedAt(analyzedAt);

        given(repository.findByIgMediaId("media123"))
                .willReturn(Optional.of(entity));

        EngagementAnalysis result = adapter.findByIgMediaId("media123").orElseThrow();

        then(result.getId()).isEqualTo(id);
        then(result.getIgMediaId()).isEqualTo("media123");
        then(result.getCompetitorUsername()).isEqualTo("fotografik_waw");
        then(result.getEngagementRate()).isEqualTo(4.5);
        then(result.getDeltaVsRollingAvg()).isEqualTo(1.2);
        then(result.getLevel()).isEqualTo(EngagementAnalysis.EngagementLevel.HIGH);
        then(result.getAnalyzedAt()).isEqualTo(analyzedAt);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private EngagementAnalysis anAnalysis() {
        return EngagementAnalysis.builder()
                .igMediaId("media123")
                .competitorUsername("fotografik_waw")
                .engagementRate(4.5)
                .deltaVsRollingAvg(1.2)
                .level(EngagementAnalysis.EngagementLevel.HIGH)
                .build();
    }

    private EngagementAnalysisEntity anEntity() {
        EngagementAnalysisEntity entity = new EngagementAnalysisEntity();
        entity.setId(UUID.randomUUID());
        entity.setIgMediaId("media123");
        entity.setCompetitorUsername("fotografik_waw");
        entity.setEngagementRate(4.5);
        entity.setDeltaVsRollingAvg(1.2);
        entity.setLevel("HIGH");
        entity.setAnalyzedAt(Instant.now());
        return entity;
    }
}