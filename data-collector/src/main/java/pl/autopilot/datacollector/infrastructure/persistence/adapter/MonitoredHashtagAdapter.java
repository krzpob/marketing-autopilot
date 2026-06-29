package pl.autopilot.datacollector.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredHashtagEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.MonitoredHashtagJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MonitoredHashtagAdapter implements MonitoredHashtagPort {

    private final MonitoredHashtagJpaRepository repository;

    @Override
    public void save(MonitoredHashtag hashtag) {
        repository.save(toEntity(hashtag));
    }

    @Override
    public List<MonitoredHashtag> findAllActive() {
        return repository.findAllByActiveTrue()
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<MonitoredHashtag> findAllActiveByHashtag(String hashtag) {
        return repository.findAllByHashtagAndActiveTrue(hashtag)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<MonitoredHashtag> findByOwnerIgIdAndHashtag(String ownerIgId,
                                                                  String hashtag) {
        return repository.findByOwnerIgIdAndHashtag(ownerIgId, hashtag)
                .map(this::toDomain);
    }

    @Override
    public void updateLastCollectedAt(UUID id, Instant lastCollectedAt) {
        repository.findById(id).ifPresent(entity -> {
            entity.setLastCollectedAt(lastCollectedAt);
            repository.save(entity);
        });
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private MonitoredHashtagEntity toEntity(MonitoredHashtag domain) {
        MonitoredHashtagEntity entity = new MonitoredHashtagEntity();
        entity.setId(domain.getId());
        entity.setOwnerIgId(domain.getOwnerIgId());
        entity.setHashtag(domain.getHashtag());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastCollectedAt(domain.getLastCollectedAt());
        return entity;
    }

    private MonitoredHashtag toDomain(MonitoredHashtagEntity entity) {
        return MonitoredHashtag.builder()
                .id(entity.getId())
                .ownerIgId(entity.getOwnerIgId())
                .hashtag(entity.getHashtag())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .lastCollectedAt(entity.getLastCollectedAt())
                .build();
    }
}