package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.port.in.CollectCompetitorDataUseCase;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.domain.port.out.CompetitorEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorDataCollectionService implements CollectCompetitorDataUseCase {

    private static final int DEFAULT_LOOKBACK_DAYS = 30;

    private final MonitoredProfilePort monitoredProfilePort;
    private final AccessTokenPort      accessTokenPort;
    private final CompetitorEventPort  competitorEventPort;
    private final SocialMediaPort      socialMediaPort;

    @Override
    public void collect(String competitorIgHandle) {
        List<MonitoredProfile> observers =
                monitoredProfilePort.findAllActiveByCompetitorHandle(competitorIgHandle);

        if (observers.isEmpty()) {
            log.warn("Brak aktywnych obserwujących dla handle={}", competitorIgHandle);
            return;
        }

        MonitoredProfile chosen = pickByOldestLastCollectedAt(observers);

        Optional<AccessToken> tokenOpt =
                accessTokenPort.findByOwnerIgId(chosen.getOwnerIgId());

        if (tokenOpt.isEmpty()) {
            log.warn("Brak tokenu dla ownerIgId={}, pomijam handle={}",
                    chosen.getOwnerIgId(), competitorIgHandle);
            return;
        }

        AccessToken token = tokenOpt.get();
        Instant     since = sinceFor(chosen);

        log.info("Zbieram posty handle={} tokenOwner={} since={}",
                competitorIgHandle, token.getOwnerIgId(), since);

        List<CollectedPost> posts =
                socialMediaPort.fetchCompetitorPosts(competitorIgHandle, since, token);

        if (posts.isEmpty()) {
            log.info("Brak nowych postów dla handle={}", competitorIgHandle);
        } else {
            CompetitorProfile profile = buildProfile(competitorIgHandle);
            posts.forEach(post -> competitorEventPort.publish(post, profile));
            log.info("Opublikowano {} eventów dla handle={}", posts.size(), competitorIgHandle);
        }

        monitoredProfilePort.updateLastCollectedAt(chosen.getId(), Instant.now());
    }

    @Override
    public void collectForProfile(String ownerIgId) {
        // TODO: stub
    }

    @Override
    public void collectForHashtag(String hashtag) {
        // TODO: stub
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private MonitoredProfile pickByOldestLastCollectedAt(List<MonitoredProfile> observers) {
        return observers.stream()
                .min(Comparator.comparing(
                        p -> p.getLastCollectedAt() == null
                                ? Instant.EPOCH
                                : p.getLastCollectedAt()))
                .orElseThrow();
    }

    private Instant sinceFor(MonitoredProfile profile) {
        return profile.getLastCollectedAt() != null
                ? profile.getLastCollectedAt()
                : Instant.now().minus(DEFAULT_LOOKBACK_DAYS, ChronoUnit.DAYS);
    }

    private CompetitorProfile buildProfile(String competitorIgHandle) {
        return CompetitorProfile.builder()
                .igId("")
                .username(competitorIgHandle)
                .followerCount(0)
                .mediaCount(0)
                .businessAccount(true)
                .build();
    }
}