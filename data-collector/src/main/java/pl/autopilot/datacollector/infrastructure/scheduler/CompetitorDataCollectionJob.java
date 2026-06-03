package pl.autopilot.datacollector.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.port.in.CollectCompetitorDataUseCase;
import pl.autopilot.datacollector.domain.port.in.RefreshAccessTokenUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitorDataCollectionJob {

    private final CollectCompetitorDataUseCase collectUseCase;
    private final RefreshAccessTokenUseCase    refreshUseCase;

    /** Co 6 godzin zbiera dane o konkurencji */
    @Scheduled(cron = "${scheduler.competitor-collection.cron:0 0 */6 * * *}")
    public void collectCompetitorData() {
        log.info("START: zbieranie danych o konkurencji");
        // TODO: B2-06 — iteracja po profilach z AccessTokenPort.findAll()
        log.info("END: zbieranie danych o konkurencji");
    }

    /** Co dobę odświeża tokeny bliskie wygaśnięcia */
    @Scheduled(cron = "${scheduler.token-refresh.cron:0 0 3 * * *}")
    public void refreshTokens() {
        log.info("START: odświeżanie tokenów");
        refreshUseCase.refreshAllExpiring();
        log.info("END: odświeżanie tokenów");
    }
}