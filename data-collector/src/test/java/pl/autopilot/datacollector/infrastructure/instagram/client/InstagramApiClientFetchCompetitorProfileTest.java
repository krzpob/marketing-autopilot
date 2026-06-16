package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockingDetails;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramProfileResponse;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockingDetails;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class InstagramApiClientFetchCompetitorProfileTest extends InstagramApiClientTestBase {

    @InjectMocks
    private InstagramApiClient apiClient;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String COMPETITOR = "zmyslowaty";

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void shouldReturnCompetitorProfileWithAllFields() {
        // given
        givenGraphBaseUrl();
        given(graphClient.getRaw(
                argThat(url -> url != null && url.contains(COMPETITOR)),
                eq(InstagramApiClient.BusinessDiscoveryProfile.class)))
                .willReturn(aBusinessDiscoveryProfile(
                        "Bio fotografa", 1500L, 42));

        // when
        CompetitorProfile result = apiClient.fetchCompetitorProfile(COMPETITOR, aValidToken());

        // then
        softly.then(result.getBiography()).isEqualTo("Bio fotografa");
        softly.then(result.getFollowerCount()).isEqualTo(1500L);
        softly.then(result.getMediaCount()).isEqualTo(42);
        softly.then(result.getLastCollectedAt()).isNotNull();
    }

    @Test
    void shouldBuildCorrectUrlWithOwnerIgIdAndCompetitorUsername() {
        // given
        givenGraphBaseUrl();
        given(graphClient.getRaw(any(), eq(InstagramApiClient.BusinessDiscoveryProfile.class)))
                .willReturn(aBusinessDiscoveryProfile("bio", 100L, 10));

        // when
        apiClient.fetchCompetitorProfile(COMPETITOR, aValidToken());

        // then
        then(graphClient).should().getRaw(
                argThat(url -> url != null
                        && url.contains(OWNER_IG_ID)
                        && url.contains(COMPETITOR)
                        && url.contains("followers_count")
                        && url.contains("biography")
                        && url.contains("media_count")),
                eq(InstagramApiClient.BusinessDiscoveryProfile.class));
    }

    @Test
    void shouldHandleNullBiography() {
        // given
        givenGraphBaseUrl();
        given(graphClient.getRaw(any(), eq(InstagramApiClient.BusinessDiscoveryProfile.class)))
                .willReturn(aBusinessDiscoveryProfile(null, 500L, 20));

        // when
        CompetitorProfile result = apiClient.fetchCompetitorProfile(COMPETITOR, aValidToken());

        // then
        softly.then(result.getBiography()).isNull();
        softly.then(result.getFollowerCount()).isEqualTo(500L);
        softly.then(result.getMediaCount()).isEqualTo(20);
    }

    @Test
    void shouldPropagateExceptionWhenGraphClientThrows() {
        // given
        givenGraphBaseUrl();
        given(graphClient.getRaw(any(), any()))
                .willThrow(new InstagramApiException(anErrorDetail(100, "Invalid parameter")));

        // when / then — bez Springa @CircuitBreaker nie działa, wyjątek propaguje się wprost
        thenThrownBy(() -> apiClient.fetchCompetitorProfile(COMPETITOR, aValidToken()))
                .isInstanceOf(InstagramApiException.class)
                .hasMessageContaining("100");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private InstagramApiClient.BusinessDiscoveryProfile aBusinessDiscoveryProfile(
            String biography, long followersCount, int mediaCount) {

        InstagramProfileResponse profileResponse = new InstagramProfileResponse();
        profileResponse.setBiography(biography);
        profileResponse.setFollowersCount(followersCount);
        profileResponse.setMediaCount(mediaCount);

        InstagramApiClient.BusinessDiscoveryProfile response =
                new InstagramApiClient.BusinessDiscoveryProfile();
        response.setBusinessDiscovery(profileResponse);
        return response;
    }
}