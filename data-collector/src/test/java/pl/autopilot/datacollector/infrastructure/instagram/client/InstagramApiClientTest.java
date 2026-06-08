package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.infrastructure.instagram.mapper.InstagramMediaMapper;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramHashtagResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramHashtagStatsResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class InstagramApiClientTest {

    @Mock
    private InstagramGraphClient   graphClient;

    @Mock
    private InstagramApiProperties properties;

    @Mock
    private InstagramMediaMapper   mediaMapper;

    @InjectMocks
    private InstagramApiClient     apiClient;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── guard na null ────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenTokenIsNull() {
        thenThrownBy(() -> apiClient.fetchOwnMedia(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("AccessToken must not be null");
    }

    @Test
    void shouldThrowWhenOwnerIgIdIsBlank() {
        // given
        AccessToken tokenWithoutId = AccessToken.builder()
                .ownerIgId("")
                .ownerUsername("testuser")
                .token("some-token")
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // when / then
        thenThrownBy(() -> apiClient.fetchOwnMedia(tokenWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ownerIgId");
    }

    // ── happy path ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnMappedPostsForValidToken() {
        // given
        AccessToken token = aValidToken();

        InstagramMediaResponse.MediaItem item1 = aMediaItem("abc");
        InstagramMediaResponse.MediaItem item2 = aMediaItem("def");

        given(graphClient.fetchAllPages(any(), eq(InstagramMediaResponse.class), any(), any()))
                .willReturn(List.of(item1, item2));

        CollectedPost post1 = aPost("abc");
        CollectedPost post2 = aPost("def");

        given(mediaMapper.toDomain(item1, "12345678", "testuser")).willReturn(post1);
        given(mediaMapper.toDomain(item2, "12345678", "testuser")).willReturn(post2);

        givenGraphBaseUrl();

        // when
        List<CollectedPost> result = apiClient.fetchOwnMedia(token);

        // then
        softly.then(result).hasSize(2);
        softly.then(result).containsExactly(post1, post2);
    }

    @Test
    void shouldReturnEmptyListWhenNoMediaFound() {
        // given
        given(graphClient.fetchAllPages(any(), eq(InstagramMediaResponse.class), any(), any()))
                .willReturn(List.of());
        
        givenGraphBaseUrl();

        // when
        List<CollectedPost> result = apiClient.fetchOwnMedia(aValidToken());

        // then
        softly.then(result).isEmpty();
        then(mediaMapper).shouldHaveNoInteractions();
    }

    @Test
    void shouldDelegateToGraphClientWithCorrectOwnerIgId() {
        // given
        given(graphClient.fetchAllPages(any(), eq(InstagramMediaResponse.class), any(), any()))
                .willReturn(List.of());

        givenGraphBaseUrl();
        // when
        apiClient.fetchOwnMedia(aValidToken());

        // then — sprawdzamy że URI zawiera ownerIgId
        then(graphClient).should().fetchAllPages(
                argThat(uri -> uri.toString().contains("12345678")),
                eq(InstagramMediaResponse.class),
                any(),
                any()
        );
    }

    @Test
    void shouldPropagateInstagramApiException() {
        // given
        given(graphClient.fetchAllPages(any(), any(), any(), any()))
                .willThrow(new InstagramApiException(anErrorDetail(190, "Token expired")));

        givenGraphBaseUrl();

        // when / then
        thenThrownBy(() -> apiClient.fetchOwnMedia(aValidToken()))
                .isInstanceOf(InstagramApiException.class)
                .matches(e -> ((InstagramApiException) e).isTokenExpired());
    }

    @Test
    void shouldReturnHashtagStatsForValidHashtag() {
        // given
        givenGraphBaseUrl();

        InstagramHashtagResponse hashtagResponse = new InstagramHashtagResponse();
        InstagramHashtagResponse.HashtagData data = new InstagramHashtagResponse.HashtagData();
        data.setId("ht_123");
        hashtagResponse.setData(List.of(data));

        InstagramHashtagStatsResponse statsResponse = new InstagramHashtagStatsResponse();
        statsResponse.setId("ht_123");
        statsResponse.setName("fotografia");

        given(graphClient.get(
                argThat(uri -> uri!=null && uri.toString().contains("ig_hashtag_search")),
                eq(InstagramHashtagResponse.class)))
                .willReturn(hashtagResponse);

        given(graphClient.get(
                argThat(uri -> uri!=null && uri.toString().contains("ht_123")),
                eq(InstagramHashtagStatsResponse.class)))
                .willReturn(statsResponse);

        // when
        HashtagStats result = apiClient.fetchHashtagStats("fotografia", aValidToken());

        // then
        softly.then(result.getHashtag()).isEqualTo("fotografia");
        softly.then(result.getIgHashtagId()).isEqualTo("ht_123");
        softly.then(result.getMediaCount()).isEqualTo(0L);
    }

    @Test
    void shouldReturnNullWhenHashtagNotFound() {
        // given
        givenGraphBaseUrl();

        InstagramHashtagResponse emptyResponse = new InstagramHashtagResponse();
        emptyResponse.setData(List.of());

        given(graphClient.get(any(), eq(InstagramHashtagResponse.class)))
                .willReturn(emptyResponse);

        // when
        HashtagStats result = apiClient.fetchHashtagStats("nieistniejacy", aValidToken());

        // then
        BDDAssertions.then(result).isNull();
        then(graphClient).should(times(1)).get(any(), any());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private AccessToken aValidToken() {
        return AccessToken.builder()
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .token("long-lived-token")
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(60L * 24 * 3600))
                .build();
    }

    private InstagramMediaResponse.MediaItem aMediaItem(String shortcode) {
        InstagramMediaResponse.MediaItem item = new InstagramMediaResponse.MediaItem();
        item.setShortcode(shortcode);
        item.setMediaType("IMAGE");
        item.setTimestamp("2024-01-01T00:00:00+0000");
        return item;
    }

    private CollectedPost aPost(String shortcode) {
        return CollectedPost.builder()
                .shortcode(shortcode)
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .mediaType(CollectedPost.MediaType.IMAGE)
                .publishedAt(Instant.now())
                .build();
    }

    private pl.autopilot.datacollector.infrastructure.instagram.model
            .InstagramErrorResponse.ErrorDetail anErrorDetail(int code, String message) {
        var detail = new pl.autopilot.datacollector.infrastructure.instagram.model
                .InstagramErrorResponse.ErrorDetail();
        detail.setCode(code);
        detail.setMessage(message);
        detail.setType("OAuthException");
        detail.setFbtraceId("trace");
        return detail;
    }

    private void givenGraphBaseUrl() {
       given(properties.getGraphBaseUrl())
        .willReturn("https://graph.facebook.com/v19.0");
    }
}