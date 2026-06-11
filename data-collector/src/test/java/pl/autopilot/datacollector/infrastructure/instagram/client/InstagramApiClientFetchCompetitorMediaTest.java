package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class InstagramApiClientFetchCompetitorMediaTest extends InstagramApiClientTestBase {

    @InjectMocks
    private InstagramApiClient apiClient;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String COMPETITOR = "fotografik_waw";

    // ── walidacja argumentów ──────────────────────────────────────────────────

    @Test
    void shouldThrowWhenTokenIsNull() {
        thenThrownBy(() -> apiClient.fetchCompetitorMedia(COMPETITOR, null, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenCompetitorUsernameIsNull() {
        thenThrownBy(() -> apiClient.fetchCompetitorMedia(null, aValidToken(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenSinceIsNull() {
        thenThrownBy(() -> apiClient.fetchCompetitorMedia(COMPETITOR, aValidToken(), null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void shouldReturnOnlyPostsNewerThanSince() {
        // given
        Instant since = Instant.parse("2024-06-01T00:00:00Z");
        givenGraphBaseUrl();
        given(graphClient.fetchPage(any(), eq(InstagramApiClient.BusinessDiscoveryResponse.class)))
                .willReturn(aDiscoveryResponse(
                        List.of(
                                aMediaItem("new",  "2024-06-15T10:00:00+0000"),
                                aMediaItem("old",  "2024-05-01T00:00:00+0000")
                        ), null));
        given(mediaMapper.toDomain(
                argThat(i -> i != null && "new".equals(i.getShortcode())),
                eq(COMPETITOR), eq(COMPETITOR)))
                .willReturn(aPost("new"));

        // when
        List<CollectedPost> result = apiClient.fetchCompetitorMedia(COMPETITOR, aValidToken(), since);

        // then
        softly.then(result).hasSize(1);
        softly.then(result.get(0).getShortcode()).isEqualTo("new");
    }

    @Test
    void shouldStopPaginationWhenOldPostEncountered() {
        // given
        Instant since = Instant.parse("2024-06-01T00:00:00Z");
        URI     page2 = URI.create(GRAPH_BASE + "/" + OWNER_IG_ID + "?after=cursor1");
        givenGraphBaseUrl();

        given(graphClient.fetchPage(
                argThat(uri -> uri != null && !uri.toString().contains("after")),
                any()))
                .willReturn(aDiscoveryResponse(
                        List.of(aMediaItem("new", "2024-06-10T00:00:00+0000")),
                        "cursor1"));
        given(graphClient.nextPageUri(any(), eq("cursor1"))).willReturn(page2);
        given(graphClient.fetchPage(eq(page2), any()))
                .willReturn(aDiscoveryResponse(
                        List.of(aMediaItem("old", "2024-05-20T00:00:00+0000")),
                        "cursor2"));
        given(mediaMapper.toDomain(any(), any(), any()))
                .willReturn(aPost("new"));

        // when
        List<CollectedPost> result = apiClient.fetchCompetitorMedia(COMPETITOR, aValidToken(), since);

        // then — tylko jeden post, cursor2 nigdy nie odpytany
        BDDAssertions.then(result).hasSize(1);
        BDDMockito.then(graphClient).should(never()).fetchPage(
                argThat(uri -> uri != null && uri.toString().contains("cursor2")), any());
    }

    @Test
    void shouldStopPaginationWhenNoCursor() {
        // given
        givenGraphBaseUrl();
        given(graphClient.fetchPage(any(), any()))
                .willReturn(aDiscoveryResponse(
                        List.of(aMediaItem("only", "2024-06-15T10:00:00+0000")),
                        null));
        given(mediaMapper.toDomain(any(), any(), any()))
                .willReturn(aPost("only"));

        // when
        List<CollectedPost> result = apiClient.fetchCompetitorMedia(
                COMPETITOR, aValidToken(), Instant.parse("2024-01-01T00:00:00Z"));

        // then
        BDDAssertions.then(result).hasSize(1);
        BDDMockito.then(graphClient).should(never()).nextPageUri(any(), any());
    }

    @Test
    void shouldReturnEmptyListWhenBusinessDiscoveryIsNull() {
        // given
        givenGraphBaseUrl();
        given(graphClient.fetchPage(any(), any()))
                .willReturn(new InstagramApiClient.BusinessDiscoveryResponse());

        // when
        List<CollectedPost> result = apiClient.fetchCompetitorMedia(
                COMPETITOR, aValidToken(), Instant.parse("2024-01-01T00:00:00Z"));

        // then
        BDDAssertions.then(result).isEmpty();
        BDDMockito.then(mediaMapper).shouldHaveNoInteractions();
    }

    @Test
    void shouldReturnEmptyListWhenDataIsNull() {
        // given
        givenGraphBaseUrl();
        given(graphClient.fetchPage(any(), any()))
                .willReturn(aDiscoveryResponse(null, null));

        // when
        List<CollectedPost> result = apiClient.fetchCompetitorMedia(
                COMPETITOR, aValidToken(), Instant.parse("2024-01-01T00:00:00Z"));

        // then
        BDDAssertions.then(result).isEmpty();
        BDDMockito.then(mediaMapper).shouldHaveNoInteractions();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private InstagramApiClient.BusinessDiscoveryResponse aDiscoveryResponse(
            List<InstagramMediaResponse.MediaItem> items, String afterCursor) {

        InstagramMediaResponse media = new InstagramMediaResponse();
        media.setData(items);

        if (afterCursor != null) {
            InstagramMediaResponse.Paging.Cursors cursors =
                    new InstagramMediaResponse.Paging.Cursors();
            cursors.setAfter(afterCursor);

            InstagramMediaResponse.Paging paging = new InstagramMediaResponse.Paging();
            paging.setCursors(cursors);
            paging.setNext("http://next-page");
            media.setPaging(paging);
        }

        InstagramApiClient.BusinessDiscoveryResponse.BusinessDiscovery bd =
                new InstagramApiClient.BusinessDiscoveryResponse.BusinessDiscovery();
        bd.setMedia(media);

        InstagramApiClient.BusinessDiscoveryResponse response =
                new InstagramApiClient.BusinessDiscoveryResponse();
        response.setBusinessDiscovery(bd);
        return response;
    }
}