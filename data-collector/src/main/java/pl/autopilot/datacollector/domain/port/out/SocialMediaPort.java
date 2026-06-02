package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.HashtagStats;

import java.util.List;

public interface SocialMediaPort {

    /** Własne posty użytkownika — GET /me/media */
    List<CollectedPost> fetchOwnPosts(AccessToken token);

    /** Posty konkurenta — Business Discovery API */
    List<CollectedPost> fetchCompetitorPosts(String competitorUsername, AccessToken token);

    /** Profil konkurenta — Business Discovery API */
    CompetitorProfile fetchCompetitorProfile(String competitorUsername, AccessToken token);

    /** Statystyki hashtagу — GET /ig_hashtag_search */
    HashtagStats fetchHashtagStats(String hashtag, AccessToken token);
}