package pl.autopilot.datacollector.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.infrastructure.instagram.adapter.InstagramSocialMediaAdapter;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramOAuthClient;


@Slf4j
@RestController
@RequestMapping("/oauth/instagram")
@RequiredArgsConstructor
class InstagramOAuthController {

    private final InstagramOAuthClient oAuthClient;
    private final AccessTokenPort      accessTokenPort;
   
    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description) {

        if (error != null) {
            log.error("Błąd OAuth: {} — {}", error, error_description);
            return ResponseEntity.badRequest()
                    .body("Błąd autoryzacji: " + error_description);
        }

        try {
            AccessToken shortLived = oAuthClient.exchangeCodeForShortLivedToken(code);
            AccessToken longLived  = oAuthClient.exchangeForLongLivedToken(shortLived);
            accessTokenPort.save(longLived);

            log.info("Token zapisany dla: {} ({})",
                    longLived.getOwnerUsername(), longLived.getOwnerIgId());

            return ResponseEntity.ok(
                    "Autoryzacja zakończona sukcesem. Token ważny do: "
                    + longLived.getExpiresAt());
        } catch (Exception e) {
            log.error("Błąd wymiany kodu OAuth", e);
            return ResponseEntity.internalServerError()
                    .body("Błąd autoryzacji: " + e.getMessage());
        }
    }

    @GetMapping("/instagram/authorize")
    public RedirectView instagramAuthorize() {
        String url = oAuthClient.buildAuthorizationUrl();
        log.info("Przekierowanie do autoryzacji: {}", url);
        return new RedirectView(url);
    }
}