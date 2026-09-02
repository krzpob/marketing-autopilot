package pl.autopilot.datacollector.infrastructure.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.autopilot.datacollector.infrastructure.instagram.LinkInstagramAccountService;

@RestController
@RequestMapping("/oauth/instagram")
@RequiredArgsConstructor
public class InstagramLinkController {

    private final LinkInstagramAccountService linkInstagramAccountService;

    @PostMapping("/link")
    ResponseEntity<Void> link(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Authorization") String authorizationHeader) {
        linkInstagramAccountService.link(jwt.getSubject(), authorizationHeader.substring(7));
        return ResponseEntity.ok().build();
    }
}