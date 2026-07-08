package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class PostNicheRelevance {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String igMediaId;
    private final PostSourceType sourceType;
    private final String ownerIgId;         // fotograf dla którego liczymy wagę

    private final List<String> matchedHashtags;
    private final double weight;             // matchedHashtags.size() / hashtagi obserwowane przez fotografa

    @Builder.Default
    private final Instant computedAt = Instant.now();
}