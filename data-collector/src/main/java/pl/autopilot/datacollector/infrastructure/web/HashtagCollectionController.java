package pl.autopilot.datacollector.infrastructure.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.in.CollectHashtagDataUseCase;

@Slf4j
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class HashtagCollectionController {

    private final CollectHashtagDataUseCase collectHashtagDataUseCase;

    @PostMapping("/hashtag")
    public ResponseEntity<HashtagCollectionRequestedDto> collectHashtag(
            @RequestBody CollectHashtagRequest request) {

        log.info("Zlecono kolekcję dla hashtagu=#{} platform={}",
                request.hashtag(), request.platform());

        collectHashtagDataUseCase.collect(request.hashtag(), request.platform());

        return ResponseEntity.ok(new HashtagCollectionRequestedDto(
                request.hashtag(),
                request.platform().name(),
                "Kolekcja zakończona"
        ));
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    record CollectHashtagRequest(String hashtag, SocialMediaPlatform platform) {
        CollectHashtagRequest {
            if (platform == null) platform = SocialMediaPlatform.INSTAGRAM;
        }
    }

    record HashtagCollectionRequestedDto(String hashtag, String platform, String status) {}
}