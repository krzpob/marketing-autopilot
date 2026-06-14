package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;

import java.util.List;
import java.time.Instant;

public interface SocialMediaPort {

    SocialMediaPlatform platform();

    /** Własne posty użytkownika — GET /me/media */
    List<CollectedPost> fetchOwnPosts(AccessToken token);

    /** Posty konkurenta od podanej daty — Business Discovery API */
    List<CollectedPost> fetchCompetitorPosts(String competitorUsername,
                                            Instant since,
                                            AccessToken token);

    /** Posty konkurenta — wszystkie dostępne (deleguje z since=EPOCH) */
    default List<CollectedPost> fetchCompetitorPosts(String competitorUsername,
                                                    AccessToken token) {
        return fetchCompetitorPosts(competitorUsername, Instant.EPOCH, token);
    }
    /** Profil konkurenta — Business Discovery API */
    CompetitorProfile fetchCompetitorProfile(String competitorUsername, AccessToken token);

    /** Statystyki hashtagу — GET /ig_hashtag_search */
    HashtagStats fetchHashtagStats(String hashtag, AccessToken token);
}