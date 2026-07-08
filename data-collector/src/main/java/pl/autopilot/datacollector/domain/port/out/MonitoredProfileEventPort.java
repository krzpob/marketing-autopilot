package pl.autopilot.datacollector.domain.port.out;

import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;

public interface MonitoredProfileEventPort {
    void publish(MonitoredProfile profile, ChangeType changeType);
}