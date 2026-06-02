package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;

public interface CompetitorEventPort {

    /** Publikuje post + snapshot profilu jako event na Kafkę */
    void publish(CollectedPost post, CompetitorProfile profile);
}