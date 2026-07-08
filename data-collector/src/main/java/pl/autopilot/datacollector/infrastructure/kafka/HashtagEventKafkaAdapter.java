package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.HashtagDataEvent;
import pl.autopilot.common.event.HashtagMediaItem;
import pl.autopilot.common.event.MediaType;
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
        HashtagDataEvent event = toEvent(stats, topMedia, ownerIgId);
        boolean sent = streamBridge.send("hashtag-data-out-0", event);
        if (sent) {
            log.debug("Event hashtag opublikowany dla #{}", stats.getHashtag());
        } else {
            log.error("Błąd publikacji eventu hashtag dla #{}", stats.getHashtag());
        }
    }

    private HashtagDataEvent toEvent(HashtagStats stats,
                                      List<CollectedPost> topMedia,
                                      String ownerIgId) {
        return HashtagDataEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("HASHTAG_TOP_MEDIA_COLLECTED")
                .setSchemaVersion("1.0")
                .setSource("data-collector")
                .setCollectedAt(Instant.now())
                .setOwnerIgId(ownerIgId)
                .setHashtag(stats.getHashtag())
                .setIgHashtagId(stats.getIgHashtagId())
                .setTopMedia(topMedia.stream().map(this::toMediaItem).toList())
                .build();
    }

    private HashtagMediaItem toMediaItem(CollectedPost post) {
        return HashtagMediaItem.newBuilder()
                .setId(post.getId().toString())
                .setMediaType(MediaType.valueOf(post.getMediaType().name()))
                .setPermalink(post.getPermalink())
                .setLikeCount(post.getLikeCount())
                .setCommentsCount(post.getCommentsCount())
                .setCaption(post.getCaption())
                .setPublishedAt(post.getPublishedAt())
                .setHashtags(post.getHashtags())
                .build();
    }
}