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

import lombok.extern.slf4j.Slf4j;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.domain.port.out.CompetitorEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

@Slf4j
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class CompetitorDataCollectionServiceTest {

    @Mock
    private MonitoredProfilePort monitoredProfilePort;
    @Mock
    private AccessTokenPort      accessTokenPort;
    @Mock
    private CompetitorEventPort  competitorEventPort;
    
    private SocialMediaPort instagramSocialMediaAdapter;
    
    private CompetitorDataCollectionService service;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String HANDLE = "fotografik_waw";

    @BeforeEach
    void setUp() {

        instagramSocialMediaAdapter = mock(SocialMediaPort.class);
        given(instagramSocialMediaAdapter.platform()).willReturn(SocialMediaPlatform.INSTAGRAM);

        SocialMediaPort facebookSocialMediaAdapter = mock(SocialMediaPort.class);
        given(facebookSocialMediaAdapter.platform()).willReturn(SocialMediaPlatform.FACEBOOK);

        service = new CompetitorDataCollectionService(
                monitoredProfilePort,
                accessTokenPort,
                competitorEventPort,
                List.of(instagramSocialMediaAdapter, facebookSocialMediaAdapter)
        );
    }

    // ── brak obserwujących ────────────────────────────────────────────────────

    @Test
    void shouldDoNothingWhenNoActiveObservers() {
        // given
        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(accessTokenPort).shouldHaveNoInteractions();
        BDDMockito.then(instagramSocialMediaAdapter).should().platform();
        BDDMockito.then(instagramSocialMediaAdapter).shouldHaveNoMoreInteractions();
        BDDMockito.then(competitorEventPort).shouldHaveNoInteractions();
    }

    // ── brak tokenu ───────────────────────────────────────────────────────────

    @Test
    void shouldDoNothingWhenTokenMissing() {
        // given
        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(aProfile("owner1", null)));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.empty());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(instagramSocialMediaAdapter).should().platform();
        BDDMockito.then(instagramSocialMediaAdapter).shouldHaveNoMoreInteractions();
        BDDMockito.then(competitorEventPort).shouldHaveNoInteractions();
    }

    // ── rotacja tokenów ───────────────────────────────────────────────────────

    @Test
    void shouldPickObserverWithOldestLastCollectedAt() {
        // given
        Instant older  = Instant.parse("2024-01-01T00:00:00Z");
        Instant newer  = Instant.parse("2024-06-01T00:00:00Z");

        MonitoredProfile profileOlder = aProfile("owner_oldest", older);
        MonitoredProfile profileNewer = aProfile("owner_newest", newer);

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profileNewer, profileOlder));
        given(accessTokenPort.findByOwnerIgId("owner_oldest"))
                .willReturn(Optional.of(aToken("owner_oldest")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(eq(HANDLE), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then — token wybrany od owner_oldest
        BDDMockito.then(accessTokenPort).should()
                .findByOwnerIgId("owner_oldest");
        BDDMockito.then(accessTokenPort).should(BDDMockito.never())
                .findByOwnerIgId("owner_newest");
    }

    @Test
    void shouldPickObserverWithNullLastCollectedAtFirst() {
        // given — null traktujemy jako Instant.EPOCH (najstarszy)
        MonitoredProfile profileNull   = aProfile("owner_null",   null);
        MonitoredProfile profileRecent = aProfile("owner_recent",
                Instant.parse("2024-06-01T00:00:00Z"));

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profileRecent, profileNull));
        given(accessTokenPort.findByOwnerIgId("owner_null"))
                .willReturn(Optional.of(aToken("owner_null")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(eq(HANDLE), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(accessTokenPort).should().findByOwnerIgId("owner_null");
    }

    // ── since ─────────────────────────────────────────────────────────────────

    @Test
    void shouldUseSinceFromLastCollectedAt() {
        // given
        Instant lastCollected = Instant.parse("2024-05-01T00:00:00Z");
        MonitoredProfile profile = aProfile("owner1", lastCollected);

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(eq(HANDLE), eq(lastCollected), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(instagramSocialMediaAdapter).should()
                .fetchCompetitorPosts(eq(HANDLE), eq(lastCollected), any());
    }

    @Test
    void shouldUse30DaysLookbackWhenLastCollectedAtIsNull() {
        // given
        MonitoredProfile profile = aProfile("owner1", null);

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        Instant before = Instant.now().minus(30, ChronoUnit.DAYS);
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);
        Instant after  = Instant.now().minus(30, ChronoUnit.DAYS);

        // then — since mieści się w oknie 30 dni ± kilka ms
        ArgumentCaptor<Instant> sinceCaptor = ArgumentCaptor.forClass(Instant.class);
        BDDMockito.then(instagramSocialMediaAdapter).should()
                .fetchCompetitorPosts(eq(HANDLE), sinceCaptor.capture(), any());
        then(sinceCaptor.getValue())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    // ── publikowanie eventów ──────────────────────────────────────────────────

    @Test
    void shouldPublishEventForEachCollectedPost() {
        // given
        MonitoredProfile profile = aProfile("owner1", Instant.parse("2024-05-01T00:00:00Z"));
        CollectedPost post1 = aPost("p1");
        CollectedPost post2 = aPost("p2");
        CompetitorProfile competitorProfile = aCompetitorProfile();

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(eq(HANDLE), any(), any()))
                .willReturn(List.of(post1, post2));
        given(instagramSocialMediaAdapter.fetchCompetitorProfile(eq(HANDLE), any()))
                .willReturn(competitorProfile);

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(competitorEventPort).should()
                .publish(eq(post1), eq(competitorProfile));
        BDDMockito.then(competitorEventPort).should()
                .publish(eq(post2), eq(competitorProfile));
    }

    @Test
    void shouldNotPublishWhenNoNewPosts() {
        // given
        MonitoredProfile profile = aProfile("owner1", Instant.parse("2024-05-01T00:00:00Z"));

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then
        BDDMockito.then(competitorEventPort).shouldHaveNoInteractions();
    }

    // ── aktualizacja lastCollectedAt ──────────────────────────────────────────

    @Test
    void shouldUpdateLastCollectedAtAfterSuccessfulCollection() {
        // given
        UUID profileId = UUID.randomUUID();
        MonitoredProfile profile = aProfileWithId(profileId, "owner1",
                Instant.parse("2024-05-01T00:00:00Z"));

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        Instant before = Instant.now();
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);
        Instant after  = Instant.now();

        // then
        ArgumentCaptor<Instant> tsCaptor = ArgumentCaptor.forClass(Instant.class);
        BDDMockito.then(monitoredProfilePort).should()
                .updateLastCollectedAt(eq(profileId), tsCaptor.capture());
        then(tsCaptor.getValue())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    void shouldUpdateLastCollectedAtEvenWhenNoNewPosts() {
        // given
        UUID profileId = UUID.randomUUID();
        MonitoredProfile profile = aProfileWithId(profileId, "owner1", null);

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(instagramSocialMediaAdapter.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE, SocialMediaPlatform.INSTAGRAM);

        // then — aktualizujemy zawsze, nawet gdy brak nowych postów
        BDDMockito.then(monitoredProfilePort).should()
                .updateLastCollectedAt(eq(profileId), any(Instant.class));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredProfile aProfile(String ownerIgId, Instant lastCollectedAt) {
        return aProfileWithId(UUID.randomUUID(), ownerIgId, lastCollectedAt);
    }

    private MonitoredProfile aProfileWithId(UUID id, String ownerIgId,
                                             Instant lastCollectedAt) {
        return MonitoredProfile.builder()
                .id(id)
                .ownerIgId(ownerIgId)
                .competitorIgHandle(HANDLE)
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
                .ownerIgId("competitor_ig_id")
                .ownerUsername(HANDLE)
                .mediaType(CollectedPost.MediaType.IMAGE)
                .publishedAt(Instant.now())
                .build();
    }

    private CompetitorProfile aCompetitorProfile() {
        return CompetitorProfile.builder()
                .username(HANDLE)
                .build();
    }
}