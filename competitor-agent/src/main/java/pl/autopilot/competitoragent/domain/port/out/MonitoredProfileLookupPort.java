package pl.autopilot.competitoragent.domain.port.out;

import java.util.List;

public interface MonitoredProfileLookupPort {
    List<String> findActiveCompetitorsForOwner(String ownerIgId);
}