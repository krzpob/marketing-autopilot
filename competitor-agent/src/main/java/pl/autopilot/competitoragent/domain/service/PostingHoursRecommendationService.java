package pl.autopilot.competitoragent.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.autopilot.competitoragent.domain.model.PostingHourRecommendation;
import pl.autopilot.competitoragent.domain.model.PostingHourStats;
import pl.autopilot.competitoragent.domain.port.out.MonitoredProfileLookupPort;
import pl.autopilot.competitoragent.domain.port.out.PostingHourStatsPort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostingHoursRecommendationService {

    private final MonitoredProfileLookupPort monitoredProfileLookupPort;
    private final PostingHourStatsPort       postingHourStatsPort;

    public List<PostingHourRecommendation> recommend(String ownerIgId,
                                                       String mediaTypeFilter,
                                                       double minEngagementRate,
                                                       int limitPerMediaType) {

        List<String> competitors =
                monitoredProfileLookupPort.findActiveCompetitorsForOwner(ownerIgId);

        if (competitors.isEmpty()) {
            return List.of();
        }

        List<PostingHourStats> stats = postingHourStatsPort.findByUsernames(competitors);

        Map<String, List<PostingHourStats>> byMediaType = stats.stream()
                .filter(s -> mediaTypeFilter == null || s.getMediaType().equals(mediaTypeFilter))
                .collect(Collectors.groupingBy(PostingHourStats::getMediaType));

        return byMediaType.entrySet().stream()
                .flatMap(entry -> aggregateByHour(entry.getKey(), entry.getValue())
                        .stream()
                        .filter(r -> r.getWeightedAvgEngagementRate() >= minEngagementRate)
                        .sorted(Comparator.comparingDouble(
                                PostingHourRecommendation::getWeightedAvgEngagementRate)
                                .reversed())
                        .limit(limitPerMediaType))
                .toList();
    }

    // ── agregacja: grupuj po godzinie, licz średnią ważoną liczbą postów ─────

    private List<PostingHourRecommendation> aggregateByHour(String mediaType,
                                                              List<PostingHourStats> stats) {
        Map<Short, List<PostingHourStats>> byHour = stats.stream()
                .collect(Collectors.groupingBy(PostingHourStats::getHourOfDay));

        return byHour.entrySet().stream()
                .map(entry -> toRecommendation(mediaType, entry.getKey(), entry.getValue()))
                .toList();
    }

    private PostingHourRecommendation toRecommendation(String mediaType, short hourOfDay,
                                                         List<PostingHourStats> statsForHour) {
        int totalPostCount = statsForHour.stream()
                .mapToInt(PostingHourStats::getPostCount)
                .sum();

        double weightedSum = statsForHour.stream()
                .mapToDouble(s -> s.getAvgEngagementRate() * s.getPostCount())
                .sum();

        double weightedAvgEr = totalPostCount > 0 ? weightedSum / totalPostCount : 0.0;

        return PostingHourRecommendation.builder()
                .mediaType(mediaType)
                .hourOfDay(hourOfDay)
                .weightedAvgEngagementRate(weightedAvgEr)
                .totalPostCount(totalPostCount)
                .competitorCount(statsForHour.size())
                .build();
    }
}