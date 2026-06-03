package pl.autopilot.datacollector.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.autopilot.datacollector.domain.port.in.CollectCompetitorDataUseCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitorDataCollectionService implements CollectCompetitorDataUseCase {

    @Override
    public void collectForProfile(String ownerIgId) {
        // TODO: B2-06 — pobranie postów przez InstagramApiClient
        log.info("TODO: collectForProfile ownerIgId={}", ownerIgId);
    }

    @Override
    public void collectForHashtag(String hashtag) {
        // TODO: B2-08 — pobranie postów przez hashtag
        log.info("TODO: collectForHashtag hashtag={}", hashtag);
    }
}