package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.CompetitorDataEvent;
import pl.autopilot.common.event.MediaType;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.port.out.CompetitorEventPort;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitorEventKafkaAdapter implements CompetitorEventPort {

    private final KafkaTemplate<String, CompetitorDataEvent> kafkaTemplate;

    @Value("${kafka.topics.competitor-data:competitor-data-events}")
    private String topic;

    @Override
    public void publish(CollectedPost post, CompetitorProfile profile) {
        CompetitorDataEvent event = toEvent(post, profile);

        kafkaTemplate.send(topic, post.getOwnerIgId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Błąd publikacji eventu ownerIgId={}",
                                post.getOwnerIgId(), ex);
                    } else {
                        log.debug("Event opublikowany topic={} partition={} offset={}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    // ── mapper domain → Avro ─────────────────────────────────────────────────

    private CompetitorDataEvent toEvent(CollectedPost post, CompetitorProfile profile) {
        return CompetitorDataEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("COMPETITOR_POST_COLLECTED")
                .setSchemaVersion("1.0")
                .setSource("data-collector")
                .setCorrelationId(null)
                .setId(post.getId().toString())
                .setShortcode(post.getShortcode())
                .setOwnerIgId(post.getOwnerIgId())
                .setOwnerUsername(post.getOwnerUsername())
                .setMediaType(MediaType.valueOf(post.getMediaType().name()))
                .setCaption(post.getCaption())
                .setHashtags(post.getHashtags())
                .setMentions(post.getMentions())
                .setMediaUrl(post.getMediaUrl())
                .setPermalink(post.getPermalink())
                .setLikeCount(post.getLikeCount())
                .setCommentsCount(post.getCommentsCount())
                .setShareCount(post.getShareCount())
                .setOwnerFollowerCount(profile.getFollowerCount())
                .setOwnerMediaCount(profile.getMediaCount())
                .setPublishedAt(post.getPublishedAt().toEpochMilli())
                .setCollectedAt(Instant.now().toEpochMilli())
                .build();
    }
}