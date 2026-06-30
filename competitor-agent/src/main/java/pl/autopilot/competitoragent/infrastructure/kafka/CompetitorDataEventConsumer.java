package pl.autopilot.competitoragent.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class CompetitorDataEventConsumer {

    @Bean 
    Consumer<CompetitorDataEventDto> competitorData() {
        return event -> {
            log.info("Odebrano CompetitorDataEvent: eventId={} competitorUsername={} mediaType={} likeCount={}",
                    event.eventId(), event.ownerUsername(), event.mediaType(), event.likeCount());
            // TODO B3-03: zapis do bazy
            // TODO B3-04: uruchomienie analizy
        };
    }

    // ── DTO — tymczasowo lokalnie, do czasu przejścia na Avro ──────────────────

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
}