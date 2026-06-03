package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.port.in.RefreshAccessTokenUseCase;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService implements RefreshAccessTokenUseCase {

    private final AccessTokenPort accessTokenPort;

    @Override
    public void refreshToken(String ownerIgId) {
        accessTokenPort.findByOwnerIgId(ownerIgId)
                .filter(AccessToken::isExpiringSoon)
                .ifPresentOrElse(
                        this::doRefresh,
                        () -> log.debug("Token dla {} nie wymaga odświeżenia", ownerIgId)
                );
    }

    @Override
    public void refreshAllExpiring() {
        List<AccessToken> expiring = accessTokenPort.findAllExpiringSoon();
        log.info("Znaleziono {} tokenów do odświeżenia", expiring.size());
        expiring.forEach(this::doRefresh);
    }

    // ── logika prywatna ──────────────────────────────────────────────────────

    private void doRefresh(AccessToken token) {
        // TODO: B2-04 — wymiana tokenu przez InstagramOAuthClient
        // AccessToken refreshed = oAuthClient.refreshLongLivedToken(token);
        // accessTokenPort.save(refreshed);
        log.info("TODO: odświeżenie tokenu dla ownerIgId={}", token.getOwnerIgId());
    }
}
