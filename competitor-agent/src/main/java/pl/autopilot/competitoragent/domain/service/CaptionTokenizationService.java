package pl.autopilot.competitoragent.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.autopilot.competitoragent.domain.event.CompetitorPostSavedEvent;
import pl.autopilot.competitoragent.domain.model.AnalysisResult;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.domain.port.out.AnalysisResultPort;
import pl.autopilot.competitoragent.domain.service.analysis.CaptionTokenizer;


@Slf4j
@Service
@RequiredArgsConstructor
public class CaptionTokenizationService {

    private final CaptionTokenizer   captionTokenizer;
    private final AnalysisResultPort analysisResultPort;

    @EventListener
    public void onCompetitorPostSaved(CompetitorPostSavedEvent event) {
        CompetitorPost post = event.post();
        log.info("Tokenizuję caption dla post igMediaId={} competitor={}",
                post.getIgMediaId(), post.getCompetitorUsername());

        CaptionTokenizer.CaptionTokens tokens = captionTokenizer.tokenize(post.getCaption());

        log.info("Tokenizacja: chars={} words={} hashtags={} emojis={} hasCta={} ctaTypes={}",
                tokens.charCount(),
                tokens.wordCount(),
                tokens.hashtagCount(),
                tokens.emojiStats().totalEmojiCount(),
                tokens.ctaStats().hasCta(),
                tokens.ctaStats().detectedTypes());

        AnalysisResult result = AnalysisResult.builder()
                .triggerEventId(post.getIgMediaId())
                .competitorUsername(post.getCompetitorUsername())
                .analysisType(AnalysisResult.AnalysisType.COMPETITOR_POST)
                .status(AnalysisResult.AnalysisStatus.SUCCESS)
                .build();

        analysisResultPort.save(result);

        log.info("Zapisano wynik tokenizacji dla igMediaId={}", post.getIgMediaId());
    }
}