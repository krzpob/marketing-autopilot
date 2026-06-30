package pl.autopilot.datacollector.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramOAuthClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private final AccessTokenPort      accessTokenPort;
    private final InstagramOAuthClient oAuthClient;

    public void refreshAllExpiring() {
        List<AccessToken> expiring = accessTokenPort.findAllExpiringSoon();
        log.info("Znaleziono {} tokenów do odświeżenia", expiring.size());
        expiring.forEach(this::refreshToken);
    }

    private void refreshToken(AccessToken token) {
        if (token.isExpired()) {
            log.warn("Token już wygasł dla ownerIgId={} — wymaga ponownej autoryzacji",
                    token.getOwnerIgId());
            return;
        }
        try {
            AccessToken refreshed = oAuthClient.refreshLongLivedToken(token);
            accessTokenPort.save(refreshed);
            log.info("Token odświeżony dla ownerIgId={} ważny do={}",
                    token.getOwnerIgId(), refreshed.getExpiresAt());
        } catch (Exception e) {
            log.error("Błąd odświeżania tokenu dla ownerIgId={}: {}",
                    token.getOwnerIgId(), e.getMessage());
        }
    }
}