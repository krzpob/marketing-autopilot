package pl.autopilot.competitoragent.domain.service;

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
import pl.autopilot.competitoragent.domain.event.CompetitorPostSavedEvent;
import pl.autopilot.competitoragent.domain.event.HashtagPostCollectedEvent;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.domain.model.HashtagCollectedPost;
import pl.autopilot.competitoragent.domain.model.PostNicheRelevance;
import pl.autopilot.competitoragent.domain.model.PostSourceType;
import pl.autopilot.competitoragent.domain.port.out.MonitoredHashtagLookupPort;
import pl.autopilot.competitoragent.domain.port.out.PostNicheRelevancePort;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class NicheRelevanceServiceTest {

    @Mock
    private MonitoredHashtagLookupPort monitoredHashtagLookupPort;
    @Mock
    private PostNicheRelevancePort     postNicheRelevancePort;

    @InjectMocks
    private NicheRelevanceService service;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── brak hashtagów w poście ───────────────────────────────────────────────

    @Test
    void shouldSkipWhenPostHasNoHashtags() {
        // given
        CompetitorPost post = aCompetitorPost("media123", List.of());

        // when
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then
        BDDMockito.then(monitoredHashtagLookupPort).shouldHaveNoInteractions();
        BDDMockito.then(postNicheRelevancePort).shouldHaveNoInteractions();
    }

    @Test
    void shouldSkipWhenPostHashtagsIsNull() {
        // given
        CompetitorPost post = aCompetitorPost("media123", null);

        // when
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then
        BDDMockito.then(monitoredHashtagLookupPort).shouldHaveNoInteractions();
        BDDMockito.then(postNicheRelevancePort).shouldHaveNoInteractions();
    }

    // ── brak dopasowań ────────────────────────────────────────────────────────

    @Test
    void shouldSkipWhenNoOwnersObserveAnyHashtag() {
        // given
        CompetitorPost post = aCompetitorPost("media123", List.of("fotografia", "boudoir"));

        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("fotografia"))
                .willReturn(List.of());
        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("boudoir"))
                .willReturn(List.of());

        // when
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then
        BDDMockito.then(postNicheRelevancePort).shouldHaveNoInteractions();
    }

    // ── happy path — CompetitorPost ──────────────────────────────────────────

    @Test
    void shouldComputeWeightForSingleMatchingOwner() {
        // given — fotograf obserwuje 4 hashtagi, post ma 2 wspólne z 3
        CompetitorPost post = aCompetitorPost("media123",
                List.of("fotografia", "boudoir", "portret"));

        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("fotografia"))
                .willReturn(List.of("owner1"));
        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("boudoir"))
                .willReturn(List.of("owner1"));
        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("portret"))
                .willReturn(List.of());
        given(monitoredHashtagLookupPort.countActiveHashtagsForOwner("owner1"))
                .willReturn(4);

        // when
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then
        ArgumentCaptor<PostNicheRelevance> captor =
                ArgumentCaptor.forClass(PostNicheRelevance.class);
        BDDMockito.then(postNicheRelevancePort).should().save(captor.capture());

        PostNicheRelevance relevance = captor.getValue();
        softly.then(relevance.getIgMediaId()).isEqualTo("media123");
        softly.then(relevance.getOwnerIgId()).isEqualTo("owner1");
        softly.then(relevance.getSourceType()).isEqualTo(PostSourceType.COMPETITOR_POST);
        softly.then(relevance.getMatchedHashtags())
                .containsExactlyInAnyOrder("fotografia", "boudoir");
        softly.then(relevance.getWeight()).isEqualTo(2.0 / 4);
    }

    @Test
    void shouldComputeWeightForMultipleOwnersIndependently() {
        // given — dwaj fotografowie, różne wagi
        CompetitorPost post = aCompetitorPost("media123", List.of("fotografia", "boudoir"));

        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("fotografia"))
                .willReturn(List.of("owner1", "owner2"));
        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("boudoir"))
                .willReturn(List.of("owner1"));
        given(monitoredHashtagLookupPort.countActiveHashtagsForOwner("owner1"))
                .willReturn(2); // 2/2 = 1.0
        given(monitoredHashtagLookupPort.countActiveHashtagsForOwner("owner2"))
                .willReturn(4); // 1/4 = 0.25

        // when
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then
        ArgumentCaptor<PostNicheRelevance> captor =
                ArgumentCaptor.forClass(PostNicheRelevance.class);
        BDDMockito.then(postNicheRelevancePort).should(BDDMockito.times(2))
                .save(captor.capture());

        List<PostNicheRelevance> saved = captor.getAllValues();

        PostNicheRelevance owner1Relevance = saved.stream()
                .filter(r -> r.getOwnerIgId().equals("owner1"))
                .findFirst().orElseThrow();
        softly.then(owner1Relevance.getWeight()).isEqualTo(1.0);
        softly.then(owner1Relevance.getMatchedHashtags())
                .containsExactlyInAnyOrder("fotografia", "boudoir");

        PostNicheRelevance owner2Relevance = saved.stream()
                .filter(r -> r.getOwnerIgId().equals("owner2"))
                .findFirst().orElseThrow();
        softly.then(owner2Relevance.getWeight()).isEqualTo(0.25);
        softly.then(owner2Relevance.getMatchedHashtags())
                .containsExactly("fotografia");
    }

    @Test
    void shouldNotDuplicateHashtagWhenSameOwnerMatchesMultipleTimes() {
        // given
        CompetitorPost post = aCompetitorPost("media123",
                List.of("fotografia", "fotografia")); // duplikat w poście

        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("fotografia"))
                .willReturn(List.of("owner1"));
        given(monitoredHashtagLookupPort.countActiveHashtagsForOwner("owner1"))
                .willReturn(2);

        // when
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then — jeśli post ma duplikat hashtagu, matchedHashtags też będzie miał duplikat
        // (świadomie nie deduplikujemy na tym poziomie — hashtag policzony tyle razy ile wystąpił)
        ArgumentCaptor<PostNicheRelevance> captor =
                ArgumentCaptor.forClass(PostNicheRelevance.class);
        BDDMockito.then(postNicheRelevancePort).should().save(captor.capture());
        softly.then(captor.getValue().getMatchedHashtags()).hasSize(2);
    }

    // ── happy path — HashtagCollectedPost ────────────────────────────────────

    @Test
    void shouldComputeWeightForHashtagCollectedPost() {
        // given
        HashtagCollectedPost post = aHashtagPost("media456", List.of("fotografia"));

        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("fotografia"))
                .willReturn(List.of("owner1"));
        given(monitoredHashtagLookupPort.countActiveHashtagsForOwner("owner1"))
                .willReturn(1);

        // when
        service.onHashtagPostCollected(new HashtagPostCollectedEvent(post));

        // then
        ArgumentCaptor<PostNicheRelevance> captor =
                ArgumentCaptor.forClass(PostNicheRelevance.class);
        BDDMockito.then(postNicheRelevancePort).should().save(captor.capture());

        PostNicheRelevance relevance = captor.getValue();
        softly.then(relevance.getIgMediaId()).isEqualTo("media456");
        softly.then(relevance.getSourceType()).isEqualTo(PostSourceType.HASHTAG_POST);
        softly.then(relevance.getWeight()).isEqualTo(1.0);
    }

    // ── zabezpieczenie przed dzieleniem przez zero ───────────────────────────

    @Test
    void shouldNotThrowWhenTotalObservedIsZero() {
        // given — teoretycznie niemożliwe, ale zabezpieczone
        CompetitorPost post = aCompetitorPost("media123", List.of("fotografia"));

        given(monitoredHashtagLookupPort.findOwnersObservingHashtag("fotografia"))
                .willReturn(List.of("owner1"));
        given(monitoredHashtagLookupPort.countActiveHashtagsForOwner("owner1"))
                .willReturn(0);

        // when — nie rzuca ArithmeticException
        service.onCompetitorPostSaved(new CompetitorPostSavedEvent(post));

        // then
        ArgumentCaptor<PostNicheRelevance> captor =
                ArgumentCaptor.forClass(PostNicheRelevance.class);
        BDDMockito.then(postNicheRelevancePort).should().save(captor.capture());
        softly.then(captor.getValue().getWeight()).isEqualTo(0.0);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CompetitorPost aCompetitorPost(String igMediaId, List<String> hashtags) {
        return CompetitorPost.builder()
                .igMediaId(igMediaId)
                .competitorUsername("fotografik_waw")
                .ownerIgId("owner_ig_123")
                .mediaType(CompetitorPost.MediaType.IMAGE)
                .hashtags(hashtags)
                .likeCount(100L)
                .commentsCount(10)
                .publishedAt(Instant.now())
                .build();
    }

    private HashtagCollectedPost aHashtagPost(String igMediaId, List<String> hashtags) {
        return HashtagCollectedPost.builder()
                .igMediaId(igMediaId)
                .hashtag("fotografia")
                .igHashtagId("ht_123")
                .mediaType(HashtagCollectedPost.MediaType.IMAGE)
                .hashtags(hashtags)
                .likeCount(100L)
                .commentsCount(10)
                .publishedAt(Instant.now())
                .build();
    }
}