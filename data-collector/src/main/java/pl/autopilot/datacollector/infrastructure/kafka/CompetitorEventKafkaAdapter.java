package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.CompetitorDataEvent;
import pl.autopilot.common.event.MediaType;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.port.out.CompetitorEventPort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitorEventKafkaAdapter implements CompetitorEventPort {

    private final StreamBridge streamBridge;

    @Value("${spring.cloud.stream.bindings.competitor-data-out-0.destination:competitor-data-events}")
    private String topic;

   @Override
    public void publish(CollectedPost post, CompetitorProfile profile) {
        CompetitorDataEventDto event = toDto(post, profile);
        boolean sent = streamBridge.send("competitor-data-out-0", event);
        if (sent) {
            log.debug("Event opublikowany dla ownerIgId={}", post.getOwnerIgId());
        } else {
            log.error("Błąd publikacji eventu dla ownerIgId={}", post.getOwnerIgId());
        }
    }

    // ── mapper domain → Avro ─────────────────────────────────────────────────

    private CompetitorDataEvent toEvent(CollectedPost post, CompetitorProfile profile) {
        log.info("Building event for post shortcode={} hashtags={} mentions={}",
        post.getShortcode(), post.getHashtags(), post.getMentions());
        return CompetitorDataEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("COMPETITOR_POST_COLLECTED")
                .setSchemaVersion("1.0")
                .setSource("data-collector")
                .setCorrelationId(null)
                .setId(post.getId().toString())
                .setShortcode(post.getShortcode()!=null?post.getShortcode():"")
                .setOwnerIgId(post.getOwnerIgId())
                .setOwnerUsername(post.getOwnerUsername())
                .setMediaType(MediaType.valueOf(post.getMediaType().name()))
                .setCaption(post.getCaption())
                .setHashtags(post.getHashtags() != null ? post.getHashtags() : List.of())
                .setMentions(post.getMentions() != null ? post.getMentions() : List.of())
                .setMediaUrl(post.getMediaUrl())
                .setPermalink(post.getPermalink())
                .setLikeCount(post.getLikeCount())
                .setCommentsCount(post.getCommentsCount())
                .setShareCount(post.getShareCount())
                .setOwnerFollowerCount(profile.getFollowerCount())
                .setOwnerMediaCount(profile.getMediaCount())
                .setPublishedAt(post.getPublishedAt()   )
                .setCollectedAt(Instant.now())
                .build();
    }

    private CompetitorDataEventDto toDto(CollectedPost post, CompetitorProfile profile) {
    return new CompetitorDataEventDto(
            UUID.randomUUID().toString(),
            "COMPETITOR_POST_COLLECTED",
            "1.0",
            "data-collector",
            post.getId().toString(),
            post.getShortcode() != null ? post.getShortcode() : "",
            post.getOwnerIgId(),
            post.getOwnerUsername() != null ? post.getOwnerUsername() : "",
            post.getMediaType().name(),
            post.getCaption(),
            post.getHashtags() != null ? post.getHashtags() : List.of(),
            post.getMentions() != null ? post.getMentions() : List.of(),
            post.getMediaUrl(),
            post.getPermalink(),
            post.getLikeCount(),
            post.getCommentsCount(),
            post.getShareCount(),
            profile.getFollowerCount(),
            profile.getMediaCount(),
            post.getPublishedAt().toEpochMilli(),
            Instant.now().toEpochMilli()
    );
}
}

// wewnątrz CompetitorEventKafkaAdapter
record CompetitorDataEventDto(
        String eventId,
        String eventType,
        String schemaVersion,
        String source,
        String id,
        String shortcode,
        String ownerIgId,
        String ownerUsername,
        String mediaType,
        String caption,
        List<String> hashtags,
        List<String> mentions,
        String mediaUrl,
        String permalink,
        long likeCount,
        int commentsCount,
        int shareCount,
        long ownerFollowerCount,
        int ownerMediaCount,
        long publishedAt,
        long collectedAt
) {}