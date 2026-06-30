package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class AnalysisResult {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    // ── kontekst uruchomienia ─────────────────────────────────────────────────
    private final String      triggerEventId;    // eventId z CompetitorDataEvent
    private final String      competitorUsername;
    private final AnalysisType analysisType;

    // ── wyniki ────────────────────────────────────────────────────────────────
    private final EngagementAnalysis  engagementAnalysis;   // nullable
    private final HashtagPerformance  hashtagPerformance;   // nullable
    private final List<String>        topHashtags;          // hashtagi z najwyższym ER
    private final String              optimalPostingHour;   // nullable — "HH:00"

    // ── metadane ──────────────────────────────────────────────────────────────
    private final AnalysisStatus status;

    @Builder.Default
    private final Instant analyzedAt = Instant.now();

    public enum AnalysisType {
        COMPETITOR_POST,    // wyzwolony przez CompetitorDataEvent
        HASHTAG_PERFORMANCE // wyzwolony przez HashtagDataEvent
    }

    public enum AnalysisStatus {
        SUCCESS,
        PARTIAL,    // część analiz się nie powiodła
        FAILED
    }
}