package pl.autopilot.datacollector.infrastructure.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.in.CollectCompetitorDataUseCase;

@Slf4j
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CompetitorCollectionController {

    private final CollectCompetitorDataUseCase collectCompetitorDataUseCase;

    // ── B2-16: on-demand kolekcja postów konkurenta ──────────────────────────

    @PostMapping("/competitor")
    public ResponseEntity<CollectionRequestedDto> collectCompetitor(
            @RequestBody CollectCompetitorRequest request) {

        log.info("Zlecono kolekcję dla handle={}, platform={}", request.competitorUsername(), request.platform());

        collectCompetitorDataUseCase.collect(request.competitorUsername(), request.platform());

        log.info("Zakończono kolekcję dla handle={}, platform={}", request.competitorUsername(), request.platform());

        return ResponseEntity.ok(new CollectionRequestedDto(
                request.competitorUsername(),
                request.platform().name(),
                "Kolekcja zakończona"
        ));
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    record CollectCompetitorRequest(String competitorUsername, SocialMediaPlatform platform) {
        CollectCompetitorRequest {
            if (platform == null) {
                platform = SocialMediaPlatform.INSTAGRAM; // Domyślna platforma
            }
        }
    }

    record CollectionRequestedDto(String competitorUsername, String platform, String status) {}
}