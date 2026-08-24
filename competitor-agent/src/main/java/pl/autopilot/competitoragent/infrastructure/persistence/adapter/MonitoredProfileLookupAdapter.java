package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.port.out.MonitoredProfileLookupPort;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredProfileProjectionJpaRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
class MonitoredProfileLookupAdapter implements MonitoredProfileLookupPort {

    private final MonitoredProfileProjectionJpaRepository repository;

    @Override
    public List<String> findActiveCompetitorsForOwner(String ownerIgId) {
        return repository.findAllByOwnerIgIdAndActiveTrue(ownerIgId)
                .stream()
                .map(e -> e.getCompetitorIgHandle())
                .distinct()
                .toList();
    }
}