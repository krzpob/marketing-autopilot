package pl.autopilot.competitoragent.domain.port.out;

import java.util.List;

public interface MonitoredHashtagLookupPort {

    /** Fotografowie aktywnie obserwujący dany hashtag */
    List<String> findOwnersObservingHashtag(String hashtag);

    /** Liczba aktywnie obserwowanych hashtagów przez danego fotografa */
    int countActiveHashtagsForOwner(String ownerIgId);
}