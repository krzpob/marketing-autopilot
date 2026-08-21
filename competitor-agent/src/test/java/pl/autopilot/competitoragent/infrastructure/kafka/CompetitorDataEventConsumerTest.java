package pl.autopilot.competitoragent.infrastructure.kafka;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.autopilot.common.event.CompetitorDataEvent;
import pl.autopilot.common.event.MediaType;
import pl.autopilot.competitoragent.domain.event.CompetitorPostSavedEvent;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.domain.port.out.CompetitorPostPort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class CompetitorDataEventConsumerTest {

    @Mock
    private CompetitorPostPort         competitorPostPort;
    @Mock
    private ApplicationEventPublisher  eventPublisher;

    @InjectMocks
    private CompetitorDataEventConsumer consumer;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void shouldSaveNewPostAndPublishEvent() {
        // given
        given(competitorPostPort.existsByIgMediaId("media123")).willReturn(false);

        CompetitorDataEvent event = anEvent("media123");

        // when
        Consumer<CompetitorDataEvent> fn = consumer.competitorData();
        fn.accept(event);

        // then
        BDDMockito.then(competitorPostPort).should().save(any(CompetitorPost.class));
        BDDMockito.then(eventPublisher).should()
                .publishEvent(any(CompetitorPostSavedEvent.class));
    }

    @Test
    void shouldMapAllFieldsCorrectly() {
        // given
        given(competitorPostPort.existsByIgMediaId("media123")).willReturn(false);

        CompetitorDataEvent event = anEvent("media123");

        // when
        consumer.competitorData().accept(event);

        // then
        ArgumentCaptor<CompetitorPost> captor = ArgumentCaptor.forClass(CompetitorPost.class);
        BDDMockito.then(competitorPostPort).should().save(captor.capture());

        CompetitorPost post = captor.getValue();
        softly.then(post.getIgMediaId()).isEqualTo("media123");
        softly.then(post.getShortcode()).isEqualTo("ABC123");
        softly.then(post.getCompetitorUsername()).isEqualTo("fotografik_waw");
        softly.then(post.getOwnerIgId()).isEqualTo("owner_ig_123");
        softly.then(post.getMediaType()).isEqualTo(CompetitorPost.MediaType.IMAGE);
        softly.then(post.getCaption()).isEqualTo("Piękna sesja");
        softly.then(post.getHashtags()).containsExactly("fotografia");
        softly.then(post.getMediaUrl()).isEqualTo("https://media.url");
        softly.then(post.getLikeCount()).isEqualTo(100L);
        softly.then(post.getCommentsCount()).isEqualTo(10);
        softly.then(post.getFollowerCountAtCollection()).isEqualTo(5000L);
        softly.then(post.getPublishedAt()).isNotNull();
        softly.then(post.getCollectedAt()).isNotNull();
    }

    // ── idempotentność ────────────────────────────────────────────────────────

    @Test
    void shouldSkipWhenPostAlreadyExists() {
        // given
        given(competitorPostPort.existsByIgMediaId("media123")).willReturn(true);

        CompetitorDataEvent event = anEvent("media123");

        // when
        consumer.competitorData().accept(event);

        // then
        BDDMockito.then(competitorPostPort).should(BDDMockito.never()).save(any());
        BDDMockito.then(eventPublisher).shouldHaveNoInteractions();
    }

    // ── obsługa null/default wartości ─────────────────────────────────────────

    @Test
    void shouldDefaultToEmptyListWhenHashtagsNull() {
        // given
        given(competitorPostPort.existsByIgMediaId("media123")).willReturn(false);

        CompetitorDataEvent event = anEventBuilder("media123")
                .setHashtags(List.of())
                .build();

        // when
        consumer.competitorData().accept(event);

        // then
        ArgumentCaptor<CompetitorPost> captor = ArgumentCaptor.forClass(CompetitorPost.class);
        BDDMockito.then(competitorPostPort).should().save(captor.capture());
        softly.then(captor.getValue().getHashtags()).isEmpty();
    }

    // ── timestampy ────────────────────────────────────────────────────────────

    @Test
    void shouldPreserveInstantTimestamps() {
        // given
        given(competitorPostPort.existsByIgMediaId("media123")).willReturn(false);

        Instant expectedPublishedAt = Instant.parse("2024-06-15T10:00:00Z");
        Instant expectedCollectedAt = Instant.parse("2024-06-15T11:00:00Z");

        CompetitorDataEvent event = anEventBuilder("media123")
                .setPublishedAt(expectedPublishedAt)
                .setCollectedAt(expectedCollectedAt)
                .build();

        // when
        consumer.competitorData().accept(event);

        // then
        ArgumentCaptor<CompetitorPost> captor = ArgumentCaptor.forClass(CompetitorPost.class);
        BDDMockito.then(competitorPostPort).should().save(captor.capture());
        softly.then(captor.getValue().getPublishedAt()).isEqualTo(expectedPublishedAt);
        softly.then(captor.getValue().getCollectedAt()).isEqualTo(expectedCollectedAt);
    }

    // ── event lokalny niesie zapisany post ────────────────────────────────────

    @Test
    void shouldPublishEventWithSavedPost() {
        // given
        given(competitorPostPort.existsByIgMediaId("media123")).willReturn(false);

        CompetitorDataEvent event = anEvent("media123");

        // when
        consumer.competitorData().accept(event);

        // then
        ArgumentCaptor<CompetitorPostSavedEvent> captor =
                ArgumentCaptor.forClass(CompetitorPostSavedEvent.class);
        BDDMockito.then(eventPublisher).should().publishEvent(captor.capture());
        softly.then(captor.getValue().post().getIgMediaId()).isEqualTo("media123");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CompetitorDataEvent anEvent(String igMediaId) {
        return anEventBuilder(igMediaId).build();
    }

    private CompetitorDataEvent.Builder anEventBuilder(String igMediaId) {
        return CompetitorDataEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("COMPETITOR_POST_COLLECTED")
                .setSchemaVersion("1.0")
                .setSource("data-collector")
                .setCorrelationId(null)
                .setId(igMediaId)
                .setShortcode("ABC123")
                .setOwnerIgId("owner_ig_123")
                .setOwnerUsername("fotografik_waw")
                .setMediaType(MediaType.IMAGE)
                .setCaption("Piękna sesja")
                .setHashtags(List.of("fotografia"))
                .setMentions(List.of())
                .setMediaUrl("https://media.url")
                .setPermalink("https://instagram.com/p/ABC123")
                .setLikeCount(100L)
                .setCommentsCount(10)
                .setShareCount(0)
                .setOwnerFollowerCount(5000L)
                .setOwnerMediaCount(120)
                .setPublishedAt(Instant.now().minusSeconds(3600))
                .setCollectedAt(Instant.now());
    }
}