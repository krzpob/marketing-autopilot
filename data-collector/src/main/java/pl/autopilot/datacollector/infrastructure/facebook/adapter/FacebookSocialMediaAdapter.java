package pl.autopilot.datacollector.infrastructure.facebook.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.*;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;

import java.util.List;

@Slf4j
@Component("facebookSocialMediaAdapter")
@RequiredArgsConstructor
public class FacebookSocialMediaAdapter implements SocialMediaPort {

    @Override
    public List<CollectedPost> fetchOwnPosts(AccessToken token) {
        // TODO: B2-x
        return List.of();
    }

    @Override
    public List<CollectedPost> fetchCompetitorPosts(String competitorUsername, AccessToken token) {
        // TODO: B2-x
        return List.of();
    }

    @Override
    public CompetitorProfile fetchCompetitorProfile(String competitorUsername, AccessToken token) {
        // TODO: B2-x
        return null;
    }

    @Override
    public HashtagStats fetchHashtagStats(String hashtag, AccessToken token) {
        // TODO: B2-x
        return null;
    }
}