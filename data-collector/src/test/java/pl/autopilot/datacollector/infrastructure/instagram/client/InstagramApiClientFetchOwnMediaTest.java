package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class InstagramApiClientFetchOwnMediaTest extends InstagramApiClientTestBase {

    @InjectMocks
    private InstagramApiClient apiClient;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

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

    @Test
    void shouldReturnMappedPostsForValidToken() {
        // given
        givenGraphBaseUrl();
        InstagramMediaResponse.MediaItem item1 = aMediaItem("abc");
        InstagramMediaResponse.MediaItem item2 = aMediaItem("def");

        given(graphClient.fetchAllPages(any(), eq(InstagramMediaResponse.class), any(), any()))
                .willReturn(List.of(item1, item2));

        CollectedPost post1 = aPost("abc");
        CollectedPost post2 = aPost("def");

        given(mediaMapper.toDomain(item1, OWNER_IG_ID, "testuser")).willReturn(post1);
        given(mediaMapper.toDomain(item2, OWNER_IG_ID, "testuser")).willReturn(post2);

        // when
        List<CollectedPost> result = apiClient.fetchOwnMedia(aValidToken());

        // then
        softly.then(result).hasSize(2);
        softly.then(result).containsExactly(post1, post2);
    }

    @Test
    void shouldReturnEmptyListWhenNoMediaFound() {
        // given
        givenGraphBaseUrl();
        given(graphClient.fetchAllPages(any(), eq(InstagramMediaResponse.class), any(), any()))
                .willReturn(List.of());

        // when
        List<CollectedPost> result = apiClient.fetchOwnMedia(aValidToken());

        // then
        softly.then(result).isEmpty();
        then(mediaMapper).shouldHaveNoInteractions();
    }

    @Test
    void shouldDelegateToGraphClientWithCorrectOwnerIgId() {
        // given
        givenGraphBaseUrl();
        given(graphClient.fetchAllPages(any(), eq(InstagramMediaResponse.class), any(), any()))
                .willReturn(List.of());

        // when
        apiClient.fetchOwnMedia(aValidToken());

        // then
        then(graphClient).should().fetchAllPages(
                argThat(uri -> uri != null && uri.toString().contains(OWNER_IG_ID)),
                eq(InstagramMediaResponse.class),
                any(), any()
        );
    }

    @Test
    void shouldPropagateInstagramApiException() {
        // given
        givenGraphBaseUrl();
        given(graphClient.fetchAllPages(any(), any(), any(), any()))
                .willThrow(new InstagramApiException(anErrorDetail(190, "Token expired")));

        // when / then
        thenThrownBy(() -> apiClient.fetchOwnMedia(aValidToken()))
                .isInstanceOf(InstagramApiException.class)
                .matches(e -> ((InstagramApiException) e).isTokenExpired());
    }
}
