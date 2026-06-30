package pl.autopilot.datacollector.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRefreshScheduler {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "${instagram.token-refresh.cron:0 0 3 * * *}")
    public void refreshExpiringTokens() {
        log.info("Uruchamiam odświeżanie tokenów Instagram");
        refreshTokenService.refreshAllExpiring();
        log.info("Zakończono odświeżanie tokenów Instagram");
    }
}