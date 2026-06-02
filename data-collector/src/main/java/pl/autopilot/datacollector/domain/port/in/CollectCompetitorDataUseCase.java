package pl.autopilot.datacollector.domain.port.in;

public interface CollectCompetitorDataUseCase {

    void collectForProfile(String ownerIgId);

    void collectForHashtag(String hashtag);
}