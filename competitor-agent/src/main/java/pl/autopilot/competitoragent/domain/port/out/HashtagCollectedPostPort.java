package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.HashtagCollectedPost;

import java.util.Optional;

public interface HashtagCollectedPostPort {

    void save(HashtagCollectedPost post);

    boolean existsByIgMediaIdAndHashtag(String igMediaId, String hashtag);

    Optional<HashtagCollectedPost> findByIgMediaIdAndHashtag(String igMediaId, String hashtag);
}