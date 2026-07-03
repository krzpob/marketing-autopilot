package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.HashtagPerformance;

import java.util.Optional;

public interface HashtagPerformancePort {

    void save(HashtagPerformance performance);

    /** Ostatnia kolekcja dla danego hashtagu — do wyliczenia trendu */
    Optional<HashtagPerformance> findLatestByHashtag(String hashtag);
}