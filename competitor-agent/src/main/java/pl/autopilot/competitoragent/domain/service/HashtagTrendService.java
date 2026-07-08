package pl.autopilot.competitoragent.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.autopilot.competitoragent.domain.event.HashtagCollectedEvent;
import pl.autopilot.competitoragent.domain.model.AnalysisResult;
import pl.autopilot.competitoragent.domain.model.HashtagPerformance;
import pl.autopilot.competitoragent.domain.port.out.AnalysisResultPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagTrendService {

    private final AnalysisResultPort analysisResultPort;

    @EventListener
    public void onHashtagCollected(HashtagCollectedEvent event) {
        HashtagPerformance performance = event.performance();

        log.info("Analizuję trend dla #{}: trend={} score={} topMediaCount={}",
                performance.getHashtag(),
                performance.getTrend(),
                performance.getTrendScore(),
                performance.getTopMediaCount());

        if (performance.getTrend() == HashtagPerformance.TrendDirection.RISING) {
            log.info("Hashtag #{} rośnie — score={} — warto rekomendować",
                    performance.getHashtag(), performance.getTrendScore());
        } else if (performance.getTrend() == HashtagPerformance.TrendDirection.FALLING) {
            log.info("Hashtag #{} traci na popularności — score={}",
                    performance.getHashtag(), performance.getTrendScore());
        }

        AnalysisResult result = AnalysisResult.builder()
                .triggerEventId(performance.getIgHashtagId())
                .analysisType(AnalysisResult.AnalysisType.HASHTAG_PERFORMANCE)
                .status(AnalysisResult.AnalysisStatus.SUCCESS)
                .build();

        analysisResultPort.save(result);

        log.info("Zapisano wynik analizy trendu dla #{}", performance.getHashtag());
    }
}