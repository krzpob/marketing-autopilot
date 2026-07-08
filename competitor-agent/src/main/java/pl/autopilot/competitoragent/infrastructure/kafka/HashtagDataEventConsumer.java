package pl.autopilot.competitoragent.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.HashtagDataEvent;
import pl.autopilot.common.event.HashtagMediaItem;
import pl.autopilot.competitoragent.domain.event.HashtagCollectedEvent;
import pl.autopilot.competitoragent.domain.event.HashtagPostCollectedEvent;
import pl.autopilot.competitoragent.domain.model.HashtagCollectedPost;
import pl.autopilot.competitoragent.domain.model.HashtagPerformance;
import pl.autopilot.competitoragent.domain.port.out.HashtagCollectedPostPort;
import pl.autopilot.competitoragent.domain.port.out.HashtagPerformancePort;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashtagDataEventConsumer {

    private final HashtagPerformancePort    hashtagPerformancePort;
    private final ApplicationEventPublisher eventPublisher;
    private final HashtagCollectedPostPort hashtagCollectedPostPort;

    @Bean
    public Consumer<HashtagDataEvent> hashtagData() {
        return event -> {
            log.info("Odebrano HashtagDataEvent: eventId={} hashtag=#{} topMediaCount={}",
                    event.getEventId(), event.getHashtag(), event.getTopMedia().size());

            Optional<HashtagPerformance> previous =
                    hashtagPerformancePort.findLatestByHashtag(event.getHashtag().toString());

            HashtagPerformance performance = toDomain(event, previous.orElse(null));
            hashtagPerformancePort.save(performance);

            log.info("Zapisano HashtagPerformance dla #{} trend={} trendScore={}",
                    performance.getHashtag(), performance.getTrend(),
                    performance.getTrendScore());

            eventPublisher.publishEvent(new HashtagCollectedEvent(performance));
            
            savePosts(event);
        };
    }

    private void savePosts(HashtagDataEvent event) {
        for (HashtagMediaItem item : event.getTopMedia()) {
            if (hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(
                    item.getId(), event.getHashtag())) {
                continue;
            }

            HashtagCollectedPost post = toPostDomain(item, event);
            hashtagCollectedPostPort.save(post);

            eventPublisher.publishEvent(new HashtagPostCollectedEvent(post));
        }
    }

    private HashtagCollectedPost toPostDomain(HashtagMediaItem item, HashtagDataEvent event) {
        return HashtagCollectedPost.builder()
            .igMediaId(item.getId())
            .hashtag(event.getHashtag())
            .igHashtagId(event.getIgHashtagId())
            .mediaType(HashtagCollectedPost.MediaType.valueOf(
                    item.getMediaType() != null ? item.getMediaType().name() : "UNKNOWN"))
            .permalink(item.getPermalink())
            .caption(item.getCaption())
            .hashtags(item.getHashtags() != null ? item.getHashtags() : List.of())
            .likeCount(item.getLikeCount())
            .commentsCount(item.getCommentsCount())
            .publishedAt(item.getPublishedAt())
            .build();
    }

    private HashtagPerformance toDomain(HashtagDataEvent event,
                                        HashtagPerformance previous) {
        List<HashtagMediaItem> topMedia = event.getTopMedia();
        int topMediaCount = topMedia.size();

        long avgLikeCount     = medianLikes(topMedia);
        int  avgCommentsCount = medianComments(topMedia);

        double trendScore = previous != null && previous.getAvgLikeCount() > 0
                ? ((double) (avgLikeCount - previous.getAvgLikeCount())
                   / previous.getAvgLikeCount()) * 100.0
                : 0.0;

        return HashtagPerformance.builder()
                .hashtag(event.getHashtag())
                .igHashtagId(event.getIgHashtagId())
                .topMediaCount(topMediaCount)
                .avgLikeCount(avgLikeCount)
                .avgCommentsCount(avgCommentsCount)
                .trend(HashtagPerformance.TrendDirection.classify(trendScore))
                .trendScore(trendScore)
                .collectedAt(event.getCollectedAt())
                .previousCollectedAt(previous != null ? previous.getCollectedAt() : null)
                .build();
    }

    // ── mediana zamiast średniej — odporność na outliery/viral posty ──────────

    private long medianLikes(List<HashtagMediaItem> topMedia) {
        if (topMedia.isEmpty()) return 0;
        long[] sorted = topMedia.stream()
                .mapToLong(HashtagMediaItem::getLikeCount)
                .sorted()
                .toArray();
        return (long) median(sorted);
    }

    private int medianComments(List<HashtagMediaItem> topMedia) {
        if (topMedia.isEmpty()) return 0;
        long[] sorted = topMedia.stream()
                .mapToInt(HashtagMediaItem::getCommentsCount)
                .sorted()
                .asLongStream()
                .toArray();
        return (int) median(sorted);
    }

    private double median(long[] sorted) {
        int n = sorted.length;
        if (n == 0) return 0;
        if (n % 2 == 1) return sorted[n / 2];
        return (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
    }
}