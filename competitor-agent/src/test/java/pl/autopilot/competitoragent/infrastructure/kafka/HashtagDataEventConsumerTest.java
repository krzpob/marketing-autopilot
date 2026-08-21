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
import pl.autopilot.common.event.HashtagDataEvent;
import pl.autopilot.common.event.HashtagMediaItem;
import pl.autopilot.common.event.MediaType;
import pl.autopilot.competitoragent.domain.event.HashtagCollectedEvent;
import pl.autopilot.competitoragent.domain.event.HashtagPostCollectedEvent;
import pl.autopilot.competitoragent.domain.model.HashtagCollectedPost;
import pl.autopilot.competitoragent.domain.model.HashtagPerformance;
import pl.autopilot.competitoragent.domain.port.out.HashtagCollectedPostPort;
import pl.autopilot.competitoragent.domain.port.out.HashtagPerformancePort;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class HashtagDataEventConsumerTest {

    @Mock
    private HashtagPerformancePort    hashtagPerformancePort;
    @Mock
    private HashtagCollectedPostPort  hashtagCollectedPostPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private HashtagDataEventConsumer consumer;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── pierwsza kolekcja — brak poprzednich danych ──────────────────────────

    @Test
    void shouldSaveWithZeroTrendScoreWhenNoPreviousData() {
        // given
        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia",
                List.of(item(100L, 10), item(200L, 20)));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        HashtagPerformance saved = captor.getValue();
        softly.then(saved.getTrendScore()).isEqualTo(0.0);
        softly.then(saved.getTrend()).isEqualTo(HashtagPerformance.TrendDirection.STABLE);
        softly.then(saved.getPreviousCollectedAt()).isNull();
    }

    // ── mediana zamiast średniej ──────────────────────────────────────────────

    @Test
    void shouldCalculateMedianForOddNumberOfItems() {
        // given — [100, 200, 900] → mediana = 200
        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia",
                List.of(item(900L, 90), item(100L, 10), item(200L, 20)));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        softly.then(captor.getValue().getAvgLikeCount()).isEqualTo(200L);
        softly.then(captor.getValue().getAvgCommentsCount()).isEqualTo(20);
    }

    @Test
    void shouldCalculateMedianForEvenNumberOfItems() {
        // given — [100, 200, 300, 400] → mediana = 250
        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia",
                List.of(item(400L, 40), item(100L, 10), item(300L, 30), item(200L, 20)));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        softly.then(captor.getValue().getAvgLikeCount()).isEqualTo(250L);
    }

    @Test
    void shouldBeResilientToViralOutlier() {
        // given
        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(
                item(10000L, 1000),
                item(100L, 10),
                item(150L, 15),
                item(120L, 12),
                item(130L, 13)
        ));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        softly.then(captor.getValue().getAvgLikeCount()).isEqualTo(130L);
    }

    @Test
    void shouldReturnZeroMedianForEmptyTopMedia() {
        // given
        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.empty());

        HashtagDataEvent event = anEvent("fotografia", List.of());

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        softly.then(captor.getValue().getAvgLikeCount()).isZero();
        softly.then(captor.getValue().getAvgCommentsCount()).isZero();
    }

    // ── trend score względem poprzedniej kolekcji ────────────────────────────

    @Test
    void shouldCalculateRisingTrendScore() {
        // given
        HashtagPerformance previous = HashtagPerformance.builder()
                .hashtag("fotografia")
                .avgLikeCount(100L)
                .collectedAt(Instant.now().minusSeconds(3600))
                .build();

        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.of(previous));
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(item(150L, 15)));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        softly.then(captor.getValue().getTrendScore()).isEqualTo(50.0);
        softly.then(captor.getValue().getTrend())
                .isEqualTo(HashtagPerformance.TrendDirection.RISING);
        softly.then(captor.getValue().getPreviousCollectedAt())
                .isEqualTo(previous.getCollectedAt());
    }

    @Test
    void shouldCalculateFallingTrendScore() {
        // given
        HashtagPerformance previous = HashtagPerformance.builder()
                .hashtag("fotografia")
                .avgLikeCount(200L)
                .collectedAt(Instant.now().minusSeconds(3600))
                .build();

        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.of(previous));
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(item(100L, 10)));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());

        softly.then(captor.getValue().getTrendScore()).isEqualTo(-50.0);
        softly.then(captor.getValue().getTrend())
                .isEqualTo(HashtagPerformance.TrendDirection.FALLING);
    }

    @Test
    void shouldNotDivideByZeroWhenPreviousAvgLikeCountIsZero() {
        // given
        HashtagPerformance previous = HashtagPerformance.builder()
                .hashtag("fotografia")
                .avgLikeCount(0L)
                .collectedAt(Instant.now().minusSeconds(3600))
                .build();

        given(hashtagPerformancePort.findLatestByHashtag("fotografia"))
                .willReturn(Optional.of(previous));
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(item(100L, 10)));

        // when — nie rzuca ArithmeticException
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagPerformance> captor =
                ArgumentCaptor.forClass(HashtagPerformance.class);
        BDDMockito.then(hashtagPerformancePort).should().save(captor.capture());
        softly.then(captor.getValue().getTrendScore()).isEqualTo(0.0);
    }

    // ── publikacja lokalnego eventu HashtagPerformance ────────────────────────

    @Test
    void shouldPublishHashtagCollectedEventAfterSave() {
        // given
        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(item(100L, 10)));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagCollectedEvent> captor =
                ArgumentCaptor.forClass(HashtagCollectedEvent.class);
        BDDMockito.then(eventPublisher).should().publishEvent(captor.capture());
        softly.then(captor.getValue().performance().getHashtag()).isEqualTo("fotografia");
    }

    // ── zapis pojedynczych postów ──────────────────────────────────────────────

    @Test
    void shouldSaveEachPostFromTopMedia() {
        // given
        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia",
                List.of(item(100L, 10), item(200L, 20)));

        // when
        consumer.hashtagData().accept(event);

        // then
        BDDMockito.then(hashtagCollectedPostPort).should(BDDMockito.times(2))
                .save(any(HashtagCollectedPost.class));
    }

    @Test
    void shouldMapPostFieldsCorrectly() {
        // given
        HashtagMediaItem mediaItem = item(100L, 10, "fotografia", "boudoir");

        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(
                mediaItem.getId(), "fotografia"))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(mediaItem));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagCollectedPost> captor =
                ArgumentCaptor.forClass(HashtagCollectedPost.class);
        BDDMockito.then(hashtagCollectedPostPort).should().save(captor.capture());

        HashtagCollectedPost post = captor.getValue();
        softly.then(post.getIgMediaId()).isEqualTo(mediaItem.getId());
        softly.then(post.getHashtag()).isEqualTo("fotografia");
        softly.then(post.getIgHashtagId()).isEqualTo("ht_123");
        softly.then(post.getMediaType()).isEqualTo(HashtagCollectedPost.MediaType.IMAGE);
        softly.then(post.getPermalink()).isEqualTo("https://instagram.com/p/abc");
        softly.then(post.getCaption()).isEqualTo("caption");
        softly.then(post.getHashtags()).containsExactly("fotografia", "boudoir");
        softly.then(post.getLikeCount()).isEqualTo(100L);
        softly.then(post.getCommentsCount()).isEqualTo(10);
        softly.then(post.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldSkipPostWhenAlreadyExists() {
        // given
        HashtagMediaItem mediaItem = item(100L, 10);

        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(
                mediaItem.getId(), "fotografia"))
                .willReturn(true);

        HashtagDataEvent event = anEvent("fotografia", List.of(mediaItem));

        // when
        consumer.hashtagData().accept(event);

        // then
        BDDMockito.then(hashtagCollectedPostPort).should(BDDMockito.never())
                .save(any());
    }

    @Test
    void shouldPublishHashtagPostCollectedEventForEachNewPost() {
        // given
        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(any(), any()))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia",
                List.of(item(100L, 10), item(200L, 20)));

        // when
        consumer.hashtagData().accept(event);

        // then
        BDDMockito.then(eventPublisher).should(BDDMockito.times(2))
                .publishEvent(any(HashtagPostCollectedEvent.class));
    }

    @Test
    void shouldNotPublishPostEventWhenPostAlreadyExists() {
        // given
        HashtagMediaItem mediaItem = item(100L, 10);

        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(
                mediaItem.getId(), "fotografia"))
                .willReturn(true);

        HashtagDataEvent event = anEvent("fotografia", List.of(mediaItem));

        // when
        consumer.hashtagData().accept(event);

        // then
        BDDMockito.then(eventPublisher).should(BDDMockito.never())
                .publishEvent(any(HashtagPostCollectedEvent.class));
    }

    @Test
    void shouldSaveEmptyHashtagsWhenNoneProvided() {
        // given
        HashtagMediaItem mediaItem = item(100L, 10); // brak varargów → pusta lista

        given(hashtagPerformancePort.findLatestByHashtag(any()))
                .willReturn(Optional.empty());
        given(hashtagCollectedPostPort.existsByIgMediaIdAndHashtag(
                mediaItem.getId(), "fotografia"))
                .willReturn(false);

        HashtagDataEvent event = anEvent("fotografia", List.of(mediaItem));

        // when
        consumer.hashtagData().accept(event);

        // then
        ArgumentCaptor<HashtagCollectedPost> captor =
                ArgumentCaptor.forClass(HashtagCollectedPost.class);
        BDDMockito.then(hashtagCollectedPostPort).should().save(captor.capture());
        softly.then(captor.getValue().getHashtags()).isEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private HashtagDataEvent anEvent(String hashtag, List<HashtagMediaItem> topMedia) {
        return HashtagDataEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("HASHTAG_TOP_MEDIA_COLLECTED")
                .setSchemaVersion("1.0")
                .setSource("data-collector")
                .setCollectedAt(Instant.now())
                .setOwnerIgId("owner_ig_123")
                .setHashtag(hashtag)
                .setIgHashtagId("ht_123")
                .setTopMedia(topMedia)
                .build();
    }

    private HashtagMediaItem item(long likeCount, int comments, String... hashtags) {
        return HashtagMediaItem.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setMediaType(MediaType.IMAGE)
                .setPermalink("https://instagram.com/p/abc")
                .setLikeCount(likeCount)
                .setCommentsCount(comments)
                .setCaption("caption")
                .setPublishedAt(Instant.now())
                .setHashtags(hashtags != null ? List.of(hashtags) : Collections.emptyList())
                .build();
    }
}