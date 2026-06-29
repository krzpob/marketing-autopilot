package pl.autopilot.datacollector.domain.model;

import java.util.List;

public record HashtagData(
    HashtagStats stats,
    List<CollectedPost> topMedia
) {

}
