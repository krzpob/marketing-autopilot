package pl.autopilot.datacollector.infrastructure.instagram.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramApiClient;

import java.util.List;

@Slf4j
@Component("instagramSocialMediaAdapter")
@RequiredArgsConstructor
public class InstagramSocialMediaAdapter implements SocialMediaPort {

    private final InstagramApiClient apiClient;

    @Override
    public List<CollectedPost> fetchOwnPosts(AccessToken token) {
        // TODO: B2-06 — mapowanie InstagramMediaResponse → List<CollectedPost>
        return List.of();
    }

    @Override
    public List<CollectedPost> fetchCompetitorPosts(String competitorUsername,
                                                    AccessToken token) {
        // TODO: B2-07
        return List.of();
    }

    @Override
    public CompetitorProfile fetchCompetitorProfile(String competitorUsername,
                                                    AccessToken token) {
        // TODO: B2-07
        return null;
    }

    @Override
    public HashtagStats fetchHashtagStats(String hashtag, AccessToken token) {
        // TODO: B2-08
        return null;
    }
}