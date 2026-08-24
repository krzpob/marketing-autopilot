package pl.autopilot.competitoragent.domain.service;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.PostingHourRecommendation;
import pl.autopilot.competitoragent.domain.model.PostingHourStats;
import pl.autopilot.competitoragent.domain.port.out.MonitoredProfileLookupPort;
import pl.autopilot.competitoragent.domain.port.out.PostingHourStatsPort;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class PostingHoursRecommendationServiceTest {

    @Mock
    private MonitoredProfileLookupPort monitoredProfileLookupPort;
    @Mock
    private PostingHourStatsPort       postingHourStatsPort;

    @InjectMocks
    private PostingHoursRecommendationService service;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private static final String OWNER_IG_ID = "owner_123";

    // ── brak obserwowanych konkurentów ────────────────────────────────────────

    @Test
    void shouldReturnEmptyListWhenNoCompetitorsObserved() {
        // given
        given(monitoredProfileLookupPort.findActiveCompetitorsForOwner(OWNER_IG_ID))
                .willReturn(List.of());

        // when
        List<PostingHourRecommendation> result =
                service.recommend(OWNER_IG_ID, null, 0, 3);

        // then
        softly.then(result).isEmpty();
    }

    // ── średnia ważona liczbą postów ──────────────────────────────────────────

    @Test
    void shouldCalculateWeightedAverageAcrossCompetitors() {
        // given — competitor1: ER=4.0 z 10 postami, competitor2: ER=2.0 z 30 postami
        // weighted avg = (4.0*10 + 2.0*30) / 40 = (40+60)/40 = 2.5
        given(monitoredProfileLookupPort.findActiveCompetitorsForOwner(OWNER_IG_ID))
                .willReturn(List.of("competitor1", "competitor2"));
        given(postingHourStatsPort.findByUsernames(List.of("competitor1", "competitor2")))
                .willReturn(List.of(
                        aStat("competitor1", "REEL", (short) 18, 4.0, 10),
                        aStat("competitor2", "REEL", (short) 18, 2.0, 30)
                ));

        // when
        List<PostingHourRecommendation> result =
                service.recommend(OWNER_IG_ID, null, 0, 3);

        // then
        softly.then(result).hasSize(1);
        PostingHourRecommendation rec = result.get(0);
        softly.then(rec.getMediaType()).isEqualTo("REEL");
        softly.then(rec.getHourOfDay()).isEqualTo((short) 18);
        softly.then(rec.getWeightedAvgEngagementRate()).isEqualTo(2.5);
        softly.then(rec.getTotalPostCount()).isEqualTo(40);
        softly.then(rec.getCompetitorCount()).isEqualTo(2);
    }

    // ── top N per typ mediów ──────────────────────────────────────────────────

    @Test
    void shouldReturnTopNPerMediaType() {
        // given — 5 godzin dla REEL, limit=3
        given(monitoredProfileLookupPort.findActiveCompetitorsForOwner(OWNER_IG_ID))
                .willReturn(List.of("competitor1"));
        given(postingHourStatsPort.findByUsernames(List.of("competitor1")))
                .willReturn(List.of(
                        aStat("competitor1", "REEL", (short) 9,  1.0, 5),
                        aStat("competitor1", "REEL", (short) 12, 3.0, 5),
                        aStat("competitor1", "REEL", (short) 15, 5.0, 5),
                        aStat("competitor1", "REEL", (short) 18, 4.0, 5),
                        aStat("competitor1", "REEL", (short) 20, 2.0, 5)
                ));

        // when
        List<PostingHourRecommendation> result =
                service.recommend(OWNER_IG_ID, null, 0, 3);

        // then — top 3 wg ER malejąco: 15 (5.0), 18 (4.0), 12 (3.0)
        softly.then(result).hasSize(3);
        softly.then(result).extracting(PostingHourRecommendation::getHourOfDay)
                .containsExactly((short) 15, (short) 18, (short) 12);
    }

    // ── podział per typ mediów niezależnie ────────────────────────────────────

    @Test
    void shouldAggregateIndependentlyPerMediaType() {
        // given
        given(monitoredProfileLookupPort.findActiveCompetitorsForOwner(OWNER_IG_ID))
                .willReturn(List.of("competitor1"));
        given(postingHourStatsPort.findByUsernames(List.of("competitor1")))
                .willReturn(List.of(
                        aStat("competitor1", "REEL",  (short) 18, 4.0, 10),
                        aStat("competitor1", "IMAGE", (short) 20, 3.0, 10)
                ));

        // when
        List<PostingHourRecommendation> result =
                service.recommend(OWNER_IG_ID, null, 0, 3);

        // then
        softly.then(result).hasSize(2);
        softly.then(result).extracting(PostingHourRecommendation::getMediaType)
                .containsExactlyInAnyOrder("REEL", "IMAGE");
    }

    // ── filtr mediaType ───────────────────────────────────────────────────────

    @Test
    void shouldFilterByMediaTypeWhenProvided() {
        // given
        given(monitoredProfileLookupPort.findActiveCompetitorsForOwner(OWNER_IG_ID))
                .willReturn(List.of("competitor1"));
        given(postingHourStatsPort.findByUsernames(List.of("competitor1")))
                .willReturn(List.of(
                        aStat("competitor1", "REEL",  (short) 18, 4.0, 10),
                        aStat("competitor1", "IMAGE", (short) 20, 3.0, 10)
                ));

        // when
        List<PostingHourRecommendation> result =
                service.recommend(OWNER_IG_ID, "REEL", 0, 3);

        // then
        softly.then(result).hasSize(1);
        softly.then(result.get(0).getMediaType()).isEqualTo("REEL");
    }

    // ── próg minEngagementRate ────────────────────────────────────────────────

    @Test
    void shouldFilterByMinEngagementRate() {
        // given
        given(monitoredProfileLookupPort.findActiveCompetitorsForOwner(OWNER_IG_ID))
                .willReturn(List.of("competitor1"));
        given(postingHourStatsPort.findByUsernames(List.of("competitor1")))
                .willReturn(List.of(
                        aStat("competitor1", "REEL", (short) 9,  1.0, 5),
                        aStat("competitor1", "REEL", (short) 18, 4.0, 5)
                ));

        // when
        List<PostingHourRecommendation> result =
                service.recommend(OWNER_IG_ID, null, 2.0, 3);

        // then — tylko godzina 18 przekracza próg 2.0
        softly.then(result).hasSize(1);
        softly.then(result.get(0).getHourOfDay()).isEqualTo((short) 18);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PostingHourStats aStat(String competitorUsername, String mediaType,
                                    short hourOfDay, double avgEr, int postCount) {
        return PostingHourStats.builder()
                .competitorUsername(competitorUsername)
                .mediaType(mediaType)
                .hourOfDay(hourOfDay)
                .avgEngagementRate(avgEr)
                .postCount(postCount)
                .build();
    }
}