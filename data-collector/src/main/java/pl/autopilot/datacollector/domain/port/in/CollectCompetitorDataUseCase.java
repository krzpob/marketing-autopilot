package pl.autopilot.datacollector.domain.port.in;

public interface CollectCompetitorDataUseCase {

    /* Zbiera posty konkurenta dla wszystkich aktywnych obserwujących */
    void collect(String competitorIgHandle);

    void collectForProfile(String ownerIgId);

    void collectForHashtag(String hashtag);
}