package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.CompetitorProfile;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.in.CollectCompetitorDataUseCase;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.domain.port.out.CompetitorEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompetitorDataCollectionService implements CollectCompetitorDataUseCase {

    private static final int DEFAULT_LOOKBACK_DAYS = 30;

    private final MonitoredProfilePort monitoredProfilePort;
    private final AccessTokenPort      accessTokenPort;
    private final CompetitorEventPort  competitorEventPort;
    private final Map<SocialMediaPlatform, SocialMediaPort> socialMediaPorts;
    
    public CompetitorDataCollectionService(
            MonitoredProfilePort monitoredProfilePort,
            AccessTokenPort accessTokenPort,
            CompetitorEventPort competitorEventPort,
            List<SocialMediaPort> socialMediaPorts) {
        this.monitoredProfilePort = monitoredProfilePort;
        this.accessTokenPort      = accessTokenPort;
        this.competitorEventPort  = competitorEventPort;
        this.socialMediaPorts     = socialMediaPorts.stream()
                .collect(Collectors.toMap(SocialMediaPort::platform, p -> p));
    }


    @Override
    public void collect(String competitorIgHandle, SocialMediaPlatform platform) {
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

        SocialMediaPort port = socialMediaPorts.get(platform);        
        List<CollectedPost> posts =
                port.fetchCompetitorPosts(competitorIgHandle, since, token);

        if (posts.isEmpty()) {
            log.info("Brak nowych postów dla handle={}", competitorIgHandle);
        } else {
            CompetitorProfile profile = port.fetchCompetitorProfile(competitorIgHandle, token);
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

}