package pl.autopilot.competitoragent.domain.event;

import pl.autopilot.competitoragent.domain.model.HashtagPerformance;

public record HashtagCollectedEvent(HashtagPerformance performance) {}