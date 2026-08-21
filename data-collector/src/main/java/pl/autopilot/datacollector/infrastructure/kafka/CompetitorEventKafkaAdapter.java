package pl.autopilot.datacollector.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.port.out.CompetitorEventPort;
import pl.autopilot.common.event.CompetitorDataEvent;
import pl.autopilot.common.event.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitorEventKafkaAdapter implements CompetitorEventPort {

    private final StreamBridge streamBridge;

    @Override
    public void publish(CollectedPost post, CompetitorProfile profile) {
        CompetitorDataEvent event = toEvent(post, profile);
        boolean sent = streamBridge.send("competitor-data-out-0", event);
        if (sent) {
            log.debug("Event opublikowany dla ownerIgId={}", post.getOwnerIgId());
        } else {
            log.error("Błąd publikacji eventu dla ownerIgId={}", post.getOwnerIgId());
        }
    }

    private CompetitorDataEvent toEvent(CollectedPost post, CompetitorProfile profile) {
        return CompetitorDataEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("COMPETITOR_POST_COLLECTED")
                .setSchemaVersion("1.0")
                .setSource("data-collector")
                .setCorrelationId(null)
                .setId(post.getId().toString())
                .setShortcode(post.getShortcode() != null ? post.getShortcode() : "")
                .setOwnerIgId(post.getOwnerIgId())
                .setOwnerUsername(post.getOwnerUsername() != null ? post.getOwnerUsername() : "")
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
                .setPublishedAt(post.getPublishedAt())
                .setCollectedAt(Instant.now())
                .build();
    }
}