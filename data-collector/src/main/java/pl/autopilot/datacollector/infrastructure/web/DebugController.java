package pl.autopilot.datacollector.infrastructure.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.model.HashtagStats;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramApiClient;
import pl.autopilot.datacollector.infrastructure.web.DebugController.CollectionResultDto;
import pl.autopilot.datacollector.infrastructure.web.DebugController.PostSummaryDto;
import pl.autopilot.datacollector.infrastructure.web.DebugController.TokenStatusDto;

import java.time.Instant;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/debug")
@Profile("!production")          // niewidoczny na produkcji
@RequiredArgsConstructor
public class DebugController {

    private final AccessTokenPort    accessTokenPort;
    private final InstagramApiClient instagramApiClient;
    private final MonitoredProfilePort monitoredProfilePort;
    private final MonitoredHashtagPort monitoredHashtagPort;

    // ── 1. Status tokenów ────────────────────────────────────────────────────

    @GetMapping("/tokens")
    public List<TokenStatusDto> listTokens() {
        return accessTokenPort.findAll().stream()
                .map(TokenStatusDto::from)
                .toList();
    }

    @GetMapping("/tokens/{ownerIgId}")
    public TokenStatusDto getToken(@PathVariable String ownerIgId) {
        return accessTokenPort.findByOwnerIgId(ownerIgId)
                .map(TokenStatusDto::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brak tokenu dla: " + ownerIgId));
    }

    // ── 2. Kolekcja własnych postów ──────────────────────────────────────────

    @PostMapping("/collect/own/{ownerIgId}")
    public CollectionResultDto collectOwnMedia(@PathVariable String ownerIgId) {
        AccessToken token = accessTokenPort.findByOwnerIgId(ownerIgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brak tokenu dla: " + ownerIgId));

        if (token.isExpired()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token wygasł: " + token.getExpiresAt());
        }

        log.info("[DEBUG] Uruchamiam kolekcję dla ownerIgId={}", ownerIgId);
        List<CollectedPost> posts = instagramApiClient.fetchOwnMedia(token);

        return new CollectionResultDto(
                ownerIgId,
                token.getOwnerUsername(),
                posts.size(),
                posts.stream().limit(5).map(PostSummaryDto::from).toList()
        );
    }

    @GetMapping("/hashtag/{hashtag}")
    public HashtagStats fetchHashtagStats(
            @PathVariable String hashtag,
            @RequestParam String ownerIgId) {

        AccessToken token = accessTokenPort.findByOwnerIgId(ownerIgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brak tokenu dla: " + ownerIgId));

        HashtagStats stats = instagramApiClient.fetchHashtagStats(hashtag, token);

        if (stats == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Nie znaleziono hashtagу: " + hashtag);
        }
        return stats;
    }

    @GetMapping("/hashtag/{hashtag}/top-media")
    public CollectionResultDto fetchHashtagTopMedia(
            @PathVariable String hashtag,
            @RequestParam String ownerIgId) {

        AccessToken token = accessTokenPort.findByOwnerIgId(ownerIgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brak tokenu dla: " + ownerIgId));

        List<CollectedPost> posts = instagramApiClient.fetchHashtagMedia(hashtag, token);

        return new CollectionResultDto(
                ownerIgId,
                hashtag,
                posts.size(),
                posts.stream().limit(5).map(PostSummaryDto::from).toList()
        );
    }

    @PostMapping("/monitored-profiles")
    public MonitoredProfile addMonitoredProfile(
            @RequestParam String ownerIgId,
            @RequestParam String competitorHandle) {

            MonitoredProfile profile = MonitoredProfile.builder()
                    .ownerIgId(ownerIgId)
                    .competitorIgHandle(competitorHandle)
                    .build();

            monitoredProfilePort.save(profile);
            log.info("[DEBUG] Dodano profil do obserwowania: {} → {}", ownerIgId, competitorHandle);
    return profile;
    }

    @GetMapping("/monitored-profiles/{ownerIgId}")
    public List<MonitoredProfile> listMonitoredProfiles(@PathVariable String ownerIgId) {
        return monitoredProfilePort.findAllByOwnerIgId(ownerIgId);
    }

    @PostMapping("/monitored-hashtag")
    public MonitoredHashtag postMethodName(@RequestBody MonitoredHashtagRequest request) {
        MonitoredHashtag monitoredHashtag = MonitoredHashtag.builder()
            .hashtag(request.hashtag)
            .ownerIgId(request.ownerIgId)
        .build();

        monitoredHashtagPort.save(monitoredHashtag);
        return monitoredHashtag;

    }
    

    // ── DTOs ─────────────────────────────────────────────────────────────────

    record MonitoredHashtagRequest(
        String ownerIgId,
        String hashtag
    ){}

    record TokenStatusDto(
            String ownerIgId,
            String ownerUsername,
            String tokenType,
            Instant expiresAt,
            boolean expired,
            boolean expiringSoon
    ) {
        static TokenStatusDto from(AccessToken t) {
            return new TokenStatusDto(
                    t.getOwnerIgId(),
                    t.getOwnerUsername(),
                    t.getTokenType().name(),
                    t.getExpiresAt(),
                    t.isExpired(),
                    t.isExpiringSoon()
            );
        }
    }

    record CollectionResultDto(
            String ownerIgId,
            String ownerUsername,
            int totalFetched,
            List<PostSummaryDto> sample   // pierwsze 5 postów
    ) {}

    record PostSummaryDto(
            String shortcode,
            String mediaType,
            String permalink,
            long   likeCount,
            int    commentsCount,
            String  caption,
            Instant publishedAt
    ) {
        static PostSummaryDto from(CollectedPost p) {
            return new PostSummaryDto(
                    p.getShortcode(),
                    p.getMediaType().name(),
                    p.getPermalink(),
                    p.getLikeCount(),
                    p.getCommentsCount(),
                    p.getCaption(),
                    p.getPublishedAt()
            );
        }
    }
}