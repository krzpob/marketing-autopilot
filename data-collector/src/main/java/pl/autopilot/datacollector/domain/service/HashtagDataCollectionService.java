package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.HashtagData;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.in.CollectHashtagDataUseCase;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.domain.port.out.HashtagEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;
import pl.autopilot.datacollector.domain.port.out.SocialMediaPort;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HashtagDataCollectionService implements CollectHashtagDataUseCase {

    private final MonitoredHashtagPort             monitoredHashtagPort;
    private final AccessTokenPort                  accessTokenPort;
    private final HashtagEventPort                 hashtagEventPort;
    private final Map<SocialMediaPlatform, SocialMediaPort> socialMediaPorts;

    public HashtagDataCollectionService(
            MonitoredHashtagPort monitoredHashtagPort,
            AccessTokenPort accessTokenPort,
            HashtagEventPort hashtagEventPort,
            List<SocialMediaPort> socialMediaPorts) {
        this.monitoredHashtagPort = monitoredHashtagPort;
        this.accessTokenPort      = accessTokenPort;
        this.hashtagEventPort     = hashtagEventPort;
        this.socialMediaPorts     = socialMediaPorts.stream()
                .collect(Collectors.toMap(SocialMediaPort::platform, p -> p));
    }

    public void collect(String hashtag, SocialMediaPlatform platform) {
        SocialMediaPort port = socialMediaPorts.get(platform);
        if (port == null) {
            log.warn("Brak adaptera dla platformy={}", platform);
            return;
        }

        List<MonitoredHashtag> observers =
                monitoredHashtagPort.findAllActiveByHashtag(hashtag);

        if (observers.isEmpty()) {
            log.warn("Brak aktywnych obserwujących dla hashtagu=#{}", hashtag);
            return;
        }

        MonitoredHashtag chosen = pickByOldestLastCollectedAt(observers);

        Optional<AccessToken> tokenOpt =
                accessTokenPort.findByOwnerIgId(chosen.getOwnerIgId());

        if (tokenOpt.isEmpty()) {
            log.warn("Brak tokenu dla ownerIgId={}, pomijam hashtag=#{}",
                    chosen.getOwnerIgId(), hashtag);
            return;
        }

        AccessToken token = tokenOpt.get();

        log.info("Zbieram top media dla hashtagu=#{} tokenOwner={}",
                hashtag, token.getOwnerIgId());

        HashtagData data = port.fetchHashtagData(hashtag, token);

        if (data.stats() == null) {
            log.warn("Brak statystyk dla hashtagu=#{}", hashtag);
        } else if (data.topMedia().isEmpty()) {
            log.info("Brak top media dla hashtagu=#{}", hashtag);
        } else {
            hashtagEventPort.publish(data.stats(), data.topMedia(), token.getOwnerIgId());
            log.info("Opublikowano event dla hashtagu=#{} topMedia={}",
                    hashtag, data.topMedia().size());
        }

        monitoredHashtagPort.updateLastCollectedAt(chosen.getId(), Instant.now());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private MonitoredHashtag pickByOldestLastCollectedAt(List<MonitoredHashtag> observers) {
        return observers.stream()
                .min(Comparator.comparing(
                        p -> p.getLastCollectedAt() == null
                                ? Instant.EPOCH
                                : p.getLastCollectedAt()))
                .orElseThrow();
    }
}