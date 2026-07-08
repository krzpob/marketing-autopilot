package pl.autopilot.competitoragent.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.autopilot.competitoragent.domain.event.CompetitorPostSavedEvent;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.domain.model.PostingHourStats;
import pl.autopilot.competitoragent.domain.port.out.PostingHourStatsPort;

import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimalPostingHoursService {

    private final PostingHourStatsPort postingHourStatsPort;

    @EventListener
    public void onCompetitorPostSaved(CompetitorPostSavedEvent event) {
        CompetitorPost post = event.post();

        short hourOfDay = (short) post.getPublishedAt()
                .atZone(ZoneOffset.UTC)
                .getHour();

        String mediaType = post.getMediaType().name();

        log.info("Aktualizuję statystyki godzinowe dla competitor={} mediaType={} hour={}",
                post.getCompetitorUsername(), mediaType, hourOfDay);

        Optional<PostingHourStats> existing =
                postingHourStatsPort.findByUsernameAndMediaTypeAndHour(
                        post.getCompetitorUsername(), mediaType, hourOfDay);

        PostingHourStats updated = existing
                .map(stats -> stats.withNewPost(
                        post.getLikeCount(),
                        post.getCommentsCount(),
                        post.getFollowerCountAtCollection()))
                .orElseGet(() -> PostingHourStats.builder()
                        .competitorUsername(post.getCompetitorUsername())
                        .mediaType(mediaType)
                        .hourOfDay(hourOfDay)
                        .build()
                        .withNewPost(
                                post.getLikeCount(),
                                post.getCommentsCount(),
                                post.getFollowerCountAtCollection()));

        postingHourStatsPort.save(updated);

        log.info("Zaktualizowano stats: hour={} avgER={} postCount={}",
                hourOfDay, updated.getAvgEngagementRate(), updated.getPostCount());
    }
}