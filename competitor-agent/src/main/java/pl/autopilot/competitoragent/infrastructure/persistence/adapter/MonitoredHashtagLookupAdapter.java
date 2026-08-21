package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.port.out.MonitoredHashtagLookupPort;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredHashtagProjectionJpaRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
class MonitoredHashtagLookupAdapter implements MonitoredHashtagLookupPort {

    private final MonitoredHashtagProjectionJpaRepository repository;

    @Override
    public List<String> findOwnersObservingHashtag(String hashtag) {
        return repository.findAllByHashtagAndActiveTrue(hashtag)
                .stream()
                .map(e -> e.getOwnerIgId())
                .distinct()
                .toList();
    }

    @Override
    public int countActiveHashtagsForOwner(String ownerIgId) {
        return repository.findAllByOwnerIgIdAndActiveTrue(ownerIgId).size();
    }
}