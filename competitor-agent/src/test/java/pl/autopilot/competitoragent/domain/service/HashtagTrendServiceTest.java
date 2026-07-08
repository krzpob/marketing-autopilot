package pl.autopilot.competitoragent.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.event.HashtagCollectedEvent;
import pl.autopilot.competitoragent.domain.model.AnalysisResult;
import pl.autopilot.competitoragent.domain.model.HashtagPerformance;
import pl.autopilot.competitoragent.domain.port.out.AnalysisResultPort;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class HashtagTrendServiceTest {

    @Mock
    private AnalysisResultPort analysisResultPort;

    @InjectMocks
    private HashtagTrendService service;

    @Test
    void shouldSaveAnalysisResultForRisingTrend() {
        // given
        HashtagPerformance performance = aPerformance(
                HashtagPerformance.TrendDirection.RISING, 25.0);

        // when
        service.onHashtagCollected(new HashtagCollectedEvent(performance));

        // then
        BDDMockito.then(analysisResultPort).should().save(any(AnalysisResult.class));
    }

    @Test
    void shouldSaveAnalysisResultForFallingTrend() {
        // given
        HashtagPerformance performance = aPerformance(
                HashtagPerformance.TrendDirection.FALLING, -30.0);

        // when
        service.onHashtagCollected(new HashtagCollectedEvent(performance));

        // then
        BDDMockito.then(analysisResultPort).should().save(any(AnalysisResult.class));
    }

    @Test
    void shouldSaveAnalysisResultForStableTrend() {
        // given
        HashtagPerformance performance = aPerformance(
                HashtagPerformance.TrendDirection.STABLE, 0.0);

        // when
        service.onHashtagCollected(new HashtagCollectedEvent(performance));

        // then
        BDDMockito.then(analysisResultPort).should().save(any(AnalysisResult.class));
    }

    @Test
    void shouldSaveResultWithCorrectAnalysisType() {
        // given
        HashtagPerformance performance = aPerformance(
                HashtagPerformance.TrendDirection.RISING, 15.0);

        // when
        service.onHashtagCollected(new HashtagCollectedEvent(performance));

        // then
        org.mockito.ArgumentCaptor<AnalysisResult> captor =
                org.mockito.ArgumentCaptor.forClass(AnalysisResult.class);
        BDDMockito.then(analysisResultPort).should().save(captor.capture());

        org.assertj.core.api.BDDAssertions.then(captor.getValue().getAnalysisType())
                .isEqualTo(AnalysisResult.AnalysisType.HASHTAG_PERFORMANCE);
        org.assertj.core.api.BDDAssertions.then(captor.getValue().getStatus())
                .isEqualTo(AnalysisResult.AnalysisStatus.SUCCESS);
        org.assertj.core.api.BDDAssertions.then(captor.getValue().getTriggerEventId())
                .isEqualTo(performance.getIgHashtagId());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private HashtagPerformance aPerformance(HashtagPerformance.TrendDirection trend,
                                             double trendScore) {
        return HashtagPerformance.builder()
                .hashtag("fotografia")
                .igHashtagId("ht_123")
                .topMediaCount(15)
                .avgLikeCount(150L)
                .avgCommentsCount(15)
                .trend(trend)
                .trendScore(trendScore)
                .build();
    }
}