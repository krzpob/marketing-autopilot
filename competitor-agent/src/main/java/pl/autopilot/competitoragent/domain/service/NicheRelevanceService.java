package pl.autopilot.competitoragent.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.autopilot.competitoragent.domain.event.CompetitorPostSavedEvent;
import pl.autopilot.competitoragent.domain.event.HashtagPostCollectedEvent;
import pl.autopilot.competitoragent.domain.model.PostNicheRelevance;
import pl.autopilot.competitoragent.domain.model.PostSourceType;
import pl.autopilot.competitoragent.domain.port.out.MonitoredHashtagLookupPort;
import pl.autopilot.competitoragent.domain.port.out.PostNicheRelevancePort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NicheRelevanceService {

    private final MonitoredHashtagLookupPort monitoredHashtagLookupPort;
    private final PostNicheRelevancePort     postNicheRelevancePort;

    @EventListener
    public void onCompetitorPostSaved(CompetitorPostSavedEvent event) {
        computeAndSave(
                event.post().getIgMediaId(),
                PostSourceType.COMPETITOR_POST,
                event.post().getHashtags());
    }

    @EventListener
    public void onHashtagPostCollected(HashtagPostCollectedEvent event) {
        computeAndSave(
                event.post().getIgMediaId(),
                PostSourceType.HASHTAG_POST,
                event.post().getHashtags());
    }

    // ── logika liczenia wagi ─────────────────────────────────────────────────

    private void computeAndSave(String igMediaId, PostSourceType sourceType,
                                 List<String> postHashtags) {
        if (postHashtags == null || postHashtags.isEmpty()) {
            log.debug("Post igMediaId={} bez hashtagów — pomijam liczenie wagi niszy",
                    igMediaId);
            return;
        }

        // ownerIgId → lista dopasowanych hashtagów
        Map<String, List<String>> matchesByOwner = new HashMap<>();

        for (String hashtag : postHashtags) {
            List<String> owners = monitoredHashtagLookupPort
                    .findOwnersObservingHashtag(hashtag);
            for (String ownerIgId : owners) {
                matchesByOwner
                        .computeIfAbsent(ownerIgId, k -> new java.util.ArrayList<>())
                        .add(hashtag);
            }
        }

        if (matchesByOwner.isEmpty()) {
            log.debug("Post igMediaId={} nie pasuje do żadnej obserwowanej niszy",
                    igMediaId);
            return;
        }

        matchesByOwner.forEach((ownerIgId, matchedHashtags) -> {
            int totalObserved =
                    monitoredHashtagLookupPort.countActiveHashtagsForOwner(ownerIgId);

            double weight = totalObserved > 0
                    ? (double) matchedHashtags.size() / totalObserved
                    : 0.0;

            PostNicheRelevance relevance = PostNicheRelevance.builder()
                    .igMediaId(igMediaId)
                    .sourceType(sourceType)
                    .ownerIgId(ownerIgId)
                    .matchedHashtags(matchedHashtags)
                    .weight(weight)
                    .build();

            postNicheRelevancePort.save(relevance);

            log.info("Waga niszy: post={} owner={} matched={} weight={}",
                    igMediaId, ownerIgId, matchedHashtags.size(), weight);
        });
    }
}