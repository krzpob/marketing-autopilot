package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.AnalysisResult;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.AnalysisResultEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.AnalysisResultJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AnalysisResultAdapterTest {

    @Mock
    private AnalysisResultJpaRepository repository;

    @InjectMocks
    private AnalysisResultAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        adapter.save(aResult());

        BDDMockito.then(repository).should().save(any(AnalysisResultEntity.class));
    }

    // ── existsByTriggerEventId ────────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenTriggerEventExists() {
        given(repository.existsByTriggerEventId("event-123")).willReturn(true);

        then(adapter.existsByTriggerEventId("event-123")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTriggerEventNotExists() {
        given(repository.existsByTriggerEventId("unknown")).willReturn(false);

        then(adapter.existsByTriggerEventId("unknown")).isFalse();
    }

    // ── findByCompetitorUsername ──────────────────────────────────────────────

    @Test
    void shouldReturnResultsForCompetitorUsername() {
        given(repository.findByCompetitorUsernameOrderByAnalyzedAtDesc("fotografik_waw"))
                .willReturn(List.of(anEntity(), anEntity()));

        List<AnalysisResult> result = adapter.findByCompetitorUsername("fotografik_waw");

        then(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoResultsFound() {
        given(repository.findByCompetitorUsernameOrderByAnalyzedAtDesc("unknown"))
                .willReturn(List.of());

        then(adapter.findByCompetitorUsername("unknown")).isEmpty();
    }

    // ── round-trip mapping ────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        UUID    id         = UUID.randomUUID();
        Instant analyzedAt = Instant.parse("2024-06-15T10:00:00Z");

        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.setId(id);
        entity.setTriggerEventId("event-123");
        entity.setCompetitorUsername("fotografik_waw");
        entity.setAnalysisType("COMPETITOR_POST");
        entity.setTopHashtags(List.of("fotografia", "boudoir"));
        entity.setOptimalPostingHour("18:00");
        entity.setStatus("SUCCESS");
        entity.setAnalyzedAt(analyzedAt);

        given(repository.findByCompetitorUsernameOrderByAnalyzedAtDesc("fotografik_waw"))
                .willReturn(List.of(entity));

        AnalysisResult result = adapter
                .findByCompetitorUsername("fotografik_waw")
                .get(0);

        then(result.getId()).isEqualTo(id);
        then(result.getTriggerEventId()).isEqualTo("event-123");
        then(result.getCompetitorUsername()).isEqualTo("fotografik_waw");
        then(result.getAnalysisType()).isEqualTo(AnalysisResult.AnalysisType.COMPETITOR_POST);
        then(result.getTopHashtags()).containsExactly("fotografia", "boudoir");
        then(result.getOptimalPostingHour()).isEqualTo("18:00");
        then(result.getStatus()).isEqualTo(AnalysisResult.AnalysisStatus.SUCCESS);
        then(result.getAnalyzedAt()).isEqualTo(analyzedAt);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AnalysisResult aResult() {
        return AnalysisResult.builder()
                .triggerEventId("event-123")
                .competitorUsername("fotografik_waw")
                .analysisType(AnalysisResult.AnalysisType.COMPETITOR_POST)
                .status(AnalysisResult.AnalysisStatus.SUCCESS)
                .build();
    }

    private AnalysisResultEntity anEntity() {
        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.setId(UUID.randomUUID());
        entity.setTriggerEventId("event-123");
        entity.setCompetitorUsername("fotografik_waw");
        entity.setAnalysisType("COMPETITOR_POST");
        entity.setStatus("SUCCESS");
        entity.setAnalyzedAt(Instant.now());
        return entity;
    }
}