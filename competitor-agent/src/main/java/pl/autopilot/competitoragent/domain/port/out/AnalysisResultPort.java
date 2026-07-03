package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.AnalysisResult;

import java.util.List;

public interface AnalysisResultPort {

    void save(AnalysisResult result);

    boolean existsByTriggerEventId(String triggerEventId);

    List<AnalysisResult> findByCompetitorUsername(String competitorUsername);
}   