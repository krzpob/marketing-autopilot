package pl.autopilot.competitoragent.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.autopilot.common.event.CompetitorDataEvent;
import pl.autopilot.competitoragent.domain.event.CompetitorPostSavedEvent;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.domain.port.out.CompetitorPostPort;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitorDataEventConsumer {

    private final CompetitorPostPort        competitorPostPort;
    private final ApplicationEventPublisher eventPublisher;

    @Bean
    public Consumer<CompetitorDataEvent> competitorData() {
        return event -> {
            log.info("Odebrano CompetitorDataEvent: eventId={} competitor={} mediaType={}",
                    event.getEventId(), event.getOwnerUsername(), event.getMediaType());

            if (competitorPostPort.existsByIgMediaId(event.getId())) {
                log.debug("Post igMediaId={} już istnieje — pomijam", event.getId());
                return;
            }

            CompetitorPost post = toDomain(event);
            competitorPostPort.save(post);

            log.info("Zapisano post igMediaId={} competitor={}",
                    post.getIgMediaId(), post.getCompetitorUsername());

            eventPublisher.publishEvent(new CompetitorPostSavedEvent(post));
        };
    }

    private CompetitorPost toDomain(CompetitorDataEvent event) {
        return CompetitorPost.builder()
                .igMediaId(event.getId())
                .shortcode(event.getShortcode())
                .competitorUsername(event.getOwnerUsername())
                .ownerIgId(event.getOwnerIgId())
                .mediaType(CompetitorPost.MediaType.valueOf(
                        event.getMediaType() != null
                                ? event.getMediaType().name()
                                : "UNKNOWN"))
                .caption(event.getCaption())
                .hashtags(event.getHashtags() != null
                        ? event.getHashtags()
                        : List.of())
                .mediaUrl(event.getMediaUrl())
                .likeCount(event.getLikeCount())
                .commentsCount(event.getCommentsCount())
                .followerCountAtCollection(event.getOwnerFollowerCount())
                .publishedAt(event.getPublishedAt())
                .collectedAt(event.getCollectedAt())
                .build();
    }
}