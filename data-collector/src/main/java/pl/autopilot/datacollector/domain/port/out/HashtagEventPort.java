package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;

import java.util.List;

public interface HashtagEventPort {

    void publish(HashtagStats stats, List<CollectedPost> topMedia, String ownerIgId);
}