package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.EngagementAnalysis;

import java.util.List;
import java.util.Optional;

public interface EngagementAnalysisPort {

    void save(EngagementAnalysis analysis);

    Optional<EngagementAnalysis> findByIgMediaId(String igMediaId);

    List<EngagementAnalysis> findByUsername(String competitorUsername);
}