package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.HashtagPerformance;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.HashtagPerformanceEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.HashtagPerformanceJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HashtagPerformanceAdapterTest {

    @Mock
    private HashtagPerformanceJpaRepository repository;

    @InjectMocks
    private HashtagPerformanceAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        adapter.save(aPerformance());

        BDDMockito.then(repository).should().save(any(HashtagPerformanceEntity.class));
    }

    // ── findLatestByHashtag ───────────────────────────────────────────────────

    @Test
    void shouldReturnLatestPerformanceWhenFound() {
        given(repository.findTopByHashtagOrderByCollectedAtDesc("fotografia"))
                .willReturn(Optional.of(anEntity()));

        Optional<HashtagPerformance> result = adapter.findLatestByHashtag("fotografia");

        then(result).isPresent();
        then(result.get().getHashtag()).isEqualTo("fotografia");
    }

    @Test
    void shouldReturnEmptyWhenHashtagNotFound() {
        given(repository.findTopByHashtagOrderByCollectedAtDesc("unknown"))
                .willReturn(Optional.empty());

        then(adapter.findLatestByHashtag("unknown")).isEmpty();
    }

    // ── round-trip mapping ────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        UUID    id           = UUID.randomUUID();
        Instant collectedAt  = Instant.parse("2024-06-15T10:00:00Z");
        Instant previousAt   = Instant.parse("2024-06-14T10:00:00Z");

        HashtagPerformanceEntity entity = new HashtagPerformanceEntity();
        entity.setId(id);
        entity.setHashtag("fotografia");
        entity.setIgHashtagId("ht_123");
        entity.setTopMediaCount(20);
        entity.setAvgLikeCount(500L);
        entity.setAvgCommentsCount(25);
        entity.setTrend("RISING");
        entity.setTrendScore(15.5);
        entity.setCollectedAt(collectedAt);
        entity.setPreviousCollectedAt(previousAt);

        given(repository.findTopByHashtagOrderByCollectedAtDesc("fotografia"))
                .willReturn(Optional.of(entity));

        HashtagPerformance result = adapter.findLatestByHashtag("fotografia").orElseThrow();

        then(result.getId()).isEqualTo(id);
        then(result.getHashtag()).isEqualTo("fotografia");
        then(result.getIgHashtagId()).isEqualTo("ht_123");
        then(result.getTopMediaCount()).isEqualTo(20);
        then(result.getAvgLikeCount()).isEqualTo(500L);
        then(result.getAvgCommentsCount()).isEqualTo(25);
        then(result.getTrend()).isEqualTo(HashtagPerformance.TrendDirection.RISING);
        then(result.getTrendScore()).isEqualTo(15.5);
        then(result.getCollectedAt()).isEqualTo(collectedAt);
        then(result.getPreviousCollectedAt()).isEqualTo(previousAt);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private HashtagPerformance aPerformance() {
        return HashtagPerformance.builder()
                .hashtag("fotografia")
                .igHashtagId("ht_123")
                .topMediaCount(20)
                .avgLikeCount(500L)
                .avgCommentsCount(25)
                .trend(HashtagPerformance.TrendDirection.RISING)
                .trendScore(15.5)
                .build();
    }

    private HashtagPerformanceEntity anEntity() {
        HashtagPerformanceEntity entity = new HashtagPerformanceEntity();
        entity.setId(UUID.randomUUID());
        entity.setHashtag("fotografia");
        entity.setIgHashtagId("ht_123");
        entity.setTopMediaCount(20);
        entity.setAvgLikeCount(500L);
        entity.setAvgCommentsCount(25);
        entity.setTrend("RISING");
        entity.setTrendScore(15.5);
        entity.setCollectedAt(Instant.now());
        return entity;
    }
}