package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramHashtagResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramHashtagStatsResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class InstagramApiClientFetchHashtagTest extends InstagramApiClientTestBase {

    @InjectMocks
    private InstagramApiClient apiClient;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

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
                argThat(uri -> uri != null && uri.toString().contains("ig_hashtag_search")),
                eq(InstagramHashtagResponse.class)))
                .willReturn(hashtagResponse);

        given(graphClient.get(
                argThat(uri -> uri != null && uri.toString().contains("ht_123")),
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

    @Test
    void shouldReturnEmptyListWhenHashtagNotFoundForTopMedia() {
        // given
        givenGraphBaseUrl();

        InstagramHashtagResponse emptyResponse = new InstagramHashtagResponse();
        emptyResponse.setData(List.of());

        given(graphClient.get(any(), eq(InstagramHashtagResponse.class)))
                .willReturn(emptyResponse);

        // when
        List<pl.autopilot.datacollector.domain.model.CollectedPost> result =
                apiClient.fetchHashtagTopMedia("nieistniejacy", aValidToken());

        // then
        BDDAssertions.then(result).isEmpty();
        then(graphClient).should(times(1)).get(any(), any());
    }

    @Test
    void shouldReturnMappedPostsForHashtagTopMedia() {
        // given
        givenGraphBaseUrl();

        InstagramHashtagResponse hashtagResponse = new InstagramHashtagResponse();
        InstagramHashtagResponse.HashtagData data = new InstagramHashtagResponse.HashtagData();
        data.setId("ht_456");
        hashtagResponse.setData(List.of(data));

        InstagramMediaResponse mediaResponse = new InstagramMediaResponse();
        InstagramMediaResponse.MediaItem item = aMediaItem("topPost");
        mediaResponse.setData(List.of(item));

        given(graphClient.get(
                argThat(uri -> uri != null && uri.toString().contains("ig_hashtag_search")),
                eq(InstagramHashtagResponse.class)))
                .willReturn(hashtagResponse);
        given(graphClient.get(
                argThat(uri -> uri != null && uri.toString().contains("top_media")),
                eq(InstagramMediaResponse.class)))
                .willReturn(mediaResponse);
        given(mediaMapper.toDomain(eq(item), eq("ht_456"), eq("fotografia")))
                .willReturn(aPost("topPost"));

        // when
        List<pl.autopilot.datacollector.domain.model.CollectedPost> result =
                apiClient.fetchHashtagTopMedia("fotografia", aValidToken());

        // then
        BDDAssertions.then(result).hasSize(1);
        BDDAssertions.then(result.get(0).getShortcode()).isEqualTo("topPost");
    }
}