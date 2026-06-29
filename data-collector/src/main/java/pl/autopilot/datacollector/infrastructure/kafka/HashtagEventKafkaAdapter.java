package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.domain.port.out.HashtagEventPort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashtagEventKafkaAdapter implements HashtagEventPort {

    private final StreamBridge streamBridge;

    @Override
    public void publish(HashtagStats stats, List<CollectedPost> topMedia, String ownerIgId) {
        HashtagDataEventDto dto = toDto(stats, topMedia, ownerIgId);
        boolean sent = streamBridge.send("hashtag-data-out-0", dto);
        if (sent) {
            log.debug("Event hashtag opublikowany dla #{}", stats.getHashtag());
        } else {
            log.error("Błąd publikacji eventu hashtag dla #{}", stats.getHashtag());
        }
    }

    private HashtagDataEventDto toDto(HashtagStats stats,
                                       List<CollectedPost> topMedia,
                                       String ownerIgId) {
        return new HashtagDataEventDto(
                UUID.randomUUID().toString(),
                "HASHTAG_TOP_MEDIA_COLLECTED",
                "1.0",
                "data-collector",
                Instant.now().toEpochMilli(),
                ownerIgId,
                stats.getHashtag(),
                stats.getIgHashtagId(),
                topMedia.stream().map(this::toMediaItem).toList()
        );
    }

    private HashtagMediaItemDto toMediaItem(CollectedPost post) {
        return new HashtagMediaItemDto(
                post.getId().toString(),
                post.getMediaType().name(),
                post.getPermalink(),
                post.getLikeCount(),
                post.getCommentsCount(),
                post.getCaption(),
                post.getPublishedAt().toEpochMilli()
        );
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    record HashtagDataEventDto(
            String eventId,
            String eventType,
            String schemaVersion,
            String source,
            long collectedAt,
            String ownerIgId,
            String hashtag,
            String igHashtagId,
            List<HashtagMediaItemDto> topMedia
    ) {}

    record HashtagMediaItemDto(
            String id,
            String mediaType,
            String permalink,
            long likeCount,
            int commentsCount,
            String caption,
            long publishedAt
    ) {}
}