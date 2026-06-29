package pl.autopilot.datacollector.domain.service;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagData;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.domain.port.out.HashtagEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class HashtagDataCollectionServiceTest {

    @Mock
    private MonitoredHashtagPort monitoredHashtagPort;
    @Mock
    private AccessTokenPort      accessTokenPort;
    @Mock
    private HashtagEventPort     hashtagEventPort;

    private SocialMediaPort instagramAdapter;
    private HashtagDataCollectionService service;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String HASHTAG = "fotografia";

    @BeforeEach
    void setUp() {
        instagramAdapter = mock(SocialMediaPort.class);
        given(instagramAdapter.platform()).willReturn(SocialMediaPlatform.INSTAGRAM);

        SocialMediaPort facebookAdapter = mock(SocialMediaPort.class);
        given(facebookAdapter.platform()).willReturn(SocialMediaPlatform.FACEBOOK);

        service = new HashtagDataCollectionService(
                monitoredHashtagPort,
                accessTokenPort,
                hashtagEventPort,
                List.of(instagramAdapter, facebookAdapter)
        );
    }

    // ── brak obserwujących ────────────────────────────────────────────────────

    @Test
    void shouldDoNothingWhenNoActiveObservers() {
        // given
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of());

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(accessTokenPort).shouldHaveNoInteractions();
        BDDMockito.then(instagramAdapter).should().platform();
        BDDMockito.then(instagramAdapter).shouldHaveNoMoreInteractions();
        BDDMockito.then(hashtagEventPort).shouldHaveNoInteractions();
    }

    // ── brak adaptera dla platformy ───────────────────────────────────────────

    @Test
    void shouldDoNothingWhenNoPlatformAdapter() {
        // when
        service.collect(HASHTAG, SocialMediaPlatform.GOOGLE);

        // then
        BDDMockito.then(monitoredHashtagPort).shouldHaveNoInteractions();
        BDDMockito.then(hashtagEventPort).shouldHaveNoInteractions();
    }

    // ── brak tokenu ───────────────────────────────────────────────────────────

    @Test
    void shouldDoNothingWhenTokenMissing() {
        // given
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(aHashtag("owner1", null)));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.empty());

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(instagramAdapter).should().platform();
        BDDMockito.then(instagramAdapter).shouldHaveNoMoreInteractions();
        BDDMockito.then(hashtagEventPort).shouldHaveNoInteractions();
    }

    // ── rotacja tokenów ───────────────────────────────────────────────────────

    @Test
    void shouldPickObserverWithOldestLastCollectedAt() {
        // given
        Instant older = Instant.parse("2024-01-01T00:00:00Z");
        Instant newer = Instant.parse("2024-06-01T00:00:00Z");

        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(
                        aHashtag("owner_newest", newer),
                        aHashtag("owner_oldest", older)));
        given(accessTokenPort.findByOwnerIgId("owner_oldest"))
                .willReturn(Optional.of(aToken("owner_oldest")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(null, List.of()));

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(accessTokenPort).should().findByOwnerIgId("owner_oldest");
        BDDMockito.then(accessTokenPort).should(BDDMockito.never())
                .findByOwnerIgId("owner_newest");
    }

    @Test
    void shouldPickObserverWithNullLastCollectedAtFirst() {
        // given
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(
                        aHashtag("owner_recent", Instant.parse("2024-06-01T00:00:00Z")),
                        aHashtag("owner_null",   null)));
        given(accessTokenPort.findByOwnerIgId("owner_null"))
                .willReturn(Optional.of(aToken("owner_null")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(null, List.of()));

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(accessTokenPort).should().findByOwnerIgId("owner_null");
    }

    // ── publikowanie eventu ───────────────────────────────────────────────────

    @Test
    void shouldPublishEventWhenTopMediaFound() {
        // given
        HashtagStats    stats    = aHashtagStats();
        List<CollectedPost> topMedia = List.of(aPost("p1"), aPost("p2"));

        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(aHashtag("owner1",
                        Instant.parse("2024-05-01T00:00:00Z"))));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(stats, topMedia));

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(hashtagEventPort).should()
                .publish(eq(stats), eq(topMedia), eq("owner1"));
    }

    @Test
    void shouldNotPublishWhenTopMediaEmpty() {
        // given
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(aHashtag("owner1", null)));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(aHashtagStats(), List.of()));

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(hashtagEventPort).shouldHaveNoInteractions();
    }

    @Test
    void shouldNotPublishWhenStatsNull() {
        // given
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(aHashtag("owner1", null)));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(null, List.of(aPost("p1"))));

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(hashtagEventPort).shouldHaveNoInteractions();
    }

    // ── aktualizacja lastCollectedAt ──────────────────────────────────────────

    @Test
    void shouldUpdateLastCollectedAtAfterCollection() {
        // given
        UUID id = UUID.randomUUID();
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(aHashtagWithId(id, "owner1", null)));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(aHashtagStats(), List.of()));

        // when
        Instant before = Instant.now();
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);
        Instant after  = Instant.now();

        // then
        ArgumentCaptor<Instant> tsCaptor = ArgumentCaptor.forClass(Instant.class);
        BDDMockito.then(monitoredHashtagPort).should()
                .updateLastCollectedAt(eq(id), tsCaptor.capture());
        then(tsCaptor.getValue())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    void shouldUpdateLastCollectedAtEvenWhenNoTopMedia() {
        // given
        UUID id = UUID.randomUUID();
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of(aHashtagWithId(id, "owner1", null)));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramAdapter.fetchHashtagData(eq(HASHTAG), any()))
                .willReturn(new HashtagData(aHashtagStats(), List.of()));

        // when
        service.collect(HASHTAG, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(monitoredHashtagPort).should()
                .updateLastCollectedAt(eq(id), any(Instant.class));
    }

    // ── default method ────────────────────────────────────────────────────────

    @Test
    void shouldDefaultToInstagramPlatform() {
        // given
        given(monitoredHashtagPort.findAllActiveByHashtag(HASHTAG))
                .willReturn(List.of());

        // when — wywołanie bez platformy
        service.collect(HASHTAG);

        // then — odpytany Instagram adapter (platform() wywołane w konstruktorze)
        BDDMockito.then(monitoredHashtagPort).should()
                .findAllActiveByHashtag(HASHTAG);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredHashtag aHashtag(String ownerIgId, Instant lastCollectedAt) {
        return aHashtagWithId(UUID.randomUUID(), ownerIgId, lastCollectedAt);
    }

    private MonitoredHashtag aHashtagWithId(UUID id, String ownerIgId,
                                             Instant lastCollectedAt) {
        return MonitoredHashtag.builder()
                .id(id)
                .ownerIgId(ownerIgId)
                .hashtag(HASHTAG)
                .active(true)
                .lastCollectedAt(lastCollectedAt)
                .build();
    }

    private AccessToken aToken(String ownerIgId) {
        return AccessToken.builder()
                .ownerIgId(ownerIgId)
                .ownerUsername(ownerIgId)
                .token("token-" + ownerIgId)
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(60L * 24 * 3600))
                .build();
    }

    private CollectedPost aPost(String shortcode) {
        return CollectedPost.builder()
                .shortcode(shortcode)
                .ownerIgId("ht_123")
                .ownerUsername(HASHTAG)
                .mediaType(CollectedPost.MediaType.IMAGE)
                .publishedAt(Instant.now())
                .build();
    }

    private HashtagStats aHashtagStats() {
        return HashtagStats.builder()
                .hashtag(HASHTAG)
                .igHashtagId("ht_123")
                .mediaCount(0)
                .build();
    }
}