package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;

public interface MonitoredHashtagEventPort {
    void publish(MonitoredHashtag hashtag, ChangeType changeType);
}