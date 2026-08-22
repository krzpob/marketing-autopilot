package pl.autopilot.competitoragent.infrastructure.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.autopilot.competitoragent.domain.model.PostingHourRecommendation;
import pl.autopilot.competitoragent.domain.service.PostingHoursRecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/photographers")
@RequiredArgsConstructor
public class CompetitorAnalyticsController {

    private final PostingHoursRecommendationService postingHoursRecommendationService;

    @GetMapping("/{ownerIgId}/posting-hours")
    public List<PostingHourRecommendation> getPostingHours(
            @PathVariable String ownerIgId,
            @RequestParam(required = false) String mediaType,
            @RequestParam(defaultValue = "0") double minEngagementRate,
            @RequestParam(defaultValue = "3") int limit) {

        return postingHoursRecommendationService.recommend(
                ownerIgId, mediaType, minEngagementRate, limit);
    }
}