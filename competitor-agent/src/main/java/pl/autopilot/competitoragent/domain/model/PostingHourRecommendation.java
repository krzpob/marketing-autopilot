package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostingHourRecommendation {
    private final String mediaType;
    private final short  hourOfDay;
    private final double weightedAvgEngagementRate;
    private final int    totalPostCount;
    private final int    competitorCount;
}