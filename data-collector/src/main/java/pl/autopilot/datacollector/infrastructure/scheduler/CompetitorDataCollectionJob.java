package pl.autopilot.datacollector.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.port.in.CollectCompetitorDataUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitorDataCollectionJob {

    private final CollectCompetitorDataUseCase collectUseCase;
    
    /** Co 6 godzin zbiera dane o konkurencji */
    @Scheduled(cron = "${scheduler.competitor-collection.cron:0 0 */6 * * *}")
    public void collectCompetitorData() {
        log.info("START: zbieranie danych o konkurencji");
        // TODO: B2-06 — iteracja po profilach z AccessTokenPort.findAll()
        log.info("END: zbieranie danych o konkurencji");
    }

}