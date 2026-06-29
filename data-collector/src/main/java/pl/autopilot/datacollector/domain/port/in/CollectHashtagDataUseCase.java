package pl.autopilot.datacollector.domain.port.in;

import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;

public interface CollectHashtagDataUseCase {

    default void collect(String hashtag) {
        collect(hashtag, SocialMediaPlatform.INSTAGRAM);
    }

    void collect(String hashtag,SocialMediaPlatform platform);
}