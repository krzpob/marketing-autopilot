package pl.autopilot.datacollector.domain.service;

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
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
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

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class CompetitorDataCollectionServiceTest {

    @Mock
    private MonitoredProfilePort monitoredProfilePort;
    @Mock
    private AccessTokenPort      accessTokenPort;
    @Mock
    private CompetitorEventPort  competitorEventPort;
    @Mock
    private SocialMediaPort      socialMediaPort;

    @InjectMocks
    private CompetitorDataCollectionService service;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String HANDLE = "fotografik_waw";

    // ── brak obserwujących ────────────────────────────────────────────────────

    @Test
    void shouldDoNothingWhenNoActiveObservers() {
        // given
        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of());

        // when
        service.collect(HANDLE);

        // then
        BDDMockito.then(accessTokenPort).shouldHaveNoInteractions();
        BDDMockito.then(socialMediaPort).shouldHaveNoInteractions();
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
        service.collect(HANDLE);

        // then
        BDDMockito.then(socialMediaPort).shouldHaveNoInteractions();
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
        given(socialMediaPort.fetchCompetitorPosts(eq(HANDLE), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE);

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
        given(socialMediaPort.fetchCompetitorPosts(eq(HANDLE), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE);

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
        given(socialMediaPort.fetchCompetitorPosts(eq(HANDLE), eq(lastCollected), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE);

        // then
        BDDMockito.then(socialMediaPort).should()
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
        given(socialMediaPort.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        Instant before = Instant.now().minus(30, ChronoUnit.DAYS);
        service.collect(HANDLE);
        Instant after  = Instant.now().minus(30, ChronoUnit.DAYS);

        // then — since mieści się w oknie 30 dni ± kilka ms
        ArgumentCaptor<Instant> sinceCaptor = ArgumentCaptor.forClass(Instant.class);
        BDDMockito.then(socialMediaPort).should()
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

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(socialMediaPort.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of(post1, post2));

        // when
        service.collect(HANDLE);

        // then
        BDDMockito.then(competitorEventPort).should()
                .publish(eq(post1), any(CompetitorProfile.class));
        BDDMockito.then(competitorEventPort).should()
                .publish(eq(post2), any(CompetitorProfile.class));
    }

    @Test
    void shouldNotPublishWhenNoNewPosts() {
        // given
        MonitoredProfile profile = aProfile("owner1", Instant.parse("2024-05-01T00:00:00Z"));

        given(monitoredProfilePort.findAllActiveByCompetitorHandle(HANDLE))
                .willReturn(List.of(profile));
        given(accessTokenPort.findByOwnerIgId("owner1"))
                .willReturn(Optional.of(aToken("owner1")));
        given(socialMediaPort.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE);

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
        given(socialMediaPort.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        Instant before = Instant.now();
        service.collect(HANDLE);
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
        given(socialMediaPort.fetchCompetitorPosts(any(), any(), any()))
                .willReturn(List.of());

        // when
        service.collect(HANDLE);

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
}