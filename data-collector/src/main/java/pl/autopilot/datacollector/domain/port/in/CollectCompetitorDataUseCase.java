package pl.autopilot.datacollector.domain.port.in;

import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;

public interface CollectCompetitorDataUseCase {

    /* Zbiera posty konkurenta dla wszystkich aktywnych obserwujących */
    void collect(String competitorIgHandle, SocialMediaPlatform platform);

    void collectForProfile(String ownerIgId);

    void collectForHashtag(String hashtag);
}