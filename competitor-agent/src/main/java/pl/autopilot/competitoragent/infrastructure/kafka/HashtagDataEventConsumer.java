package pl.autopilot.competitoragent.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class HashtagDataEventConsumer {

    @Bean
    Consumer<HashtagDataEventDto> hashtagData() {
        return event -> {
            log.info("Odebrano HashtagDataEvent: eventId={} hashtag=#{} topMediaCount={}",
                    event.eventId(), event.hashtag(), event.topMedia().size());
            // TODO B3-03: zapis do bazy
            // TODO B3-04: uruchomienie analizy trendów
        };
    }

    // ── DTO — tymczasowo lokalnie, do czasu przejścia na Avro ──────────────────

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