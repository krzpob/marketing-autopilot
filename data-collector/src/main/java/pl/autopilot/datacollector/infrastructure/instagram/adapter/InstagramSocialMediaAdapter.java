package pl.autopilot.datacollector.infrastructure.instagram.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramApiClient;

import java.util.List;
import java.time.Instant;

@Slf4j
@Component("instagramSocialMediaAdapter")
@RequiredArgsConstructor
public class InstagramSocialMediaAdapter implements SocialMediaPort {

    private final InstagramApiClient apiClient;

    @Override
    public SocialMediaPlatform platform() {
        return SocialMediaPlatform.INSTAGRAM;
    }

    @Override
    public List<CollectedPost> fetchOwnPosts(AccessToken token) {
        return apiClient.fetchOwnMedia(token);
    }

    @Override
    public List<CollectedPost> fetchCompetitorPosts(String competitorUsername,
                                                    Instant since,
                                                    AccessToken token) {
        return apiClient.fetchCompetitorMedia(competitorUsername, token, since);
    }

    @Override
    public CompetitorProfile fetchCompetitorProfile(String competitorUsername,
                                                    AccessToken token) {
        return apiClient.fetchCompetitorProfile(competitorUsername, token);
    }

    @Override
    public HashtagStats fetchHashtagStats(String hashtag, AccessToken token) {
        HashtagStats stats   = apiClient.fetchHashtagStats(hashtag, token);
        List<CollectedPost> topMedia = apiClient.fetchHashtagTopMedia(hashtag, token);
        // top media dostępne przez osobne wywołanie — można je połączyć
        return stats;
    }
}