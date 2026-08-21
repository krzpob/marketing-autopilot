package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.PostNicheRelevance;

public interface PostNicheRelevancePort {
    void save(PostNicheRelevance relevance);
}