package pl.autopilot.datacollector.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;
import pl.autopilot.datacollector.infrastructure.persistence.entity.MonitoredProfileEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.MonitoredProfileJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MonitoredProfileAdapter implements MonitoredProfilePort {

    private final MonitoredProfileJpaRepository repository;

    @Override
    public void save(MonitoredProfile profile) {
        repository.save(toEntity(profile));
    }

    @Override
    public List<MonitoredProfile> findAllByOwnerIgId(String ownerIgId) {
        return repository.findAllByOwnerIgId(ownerIgId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<MonitoredProfile> findAllActive() {
        return repository.findAllByActiveTrue()
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<MonitoredProfile> findByOwnerIgIdAndHandle(String ownerIgId,
                                                                String competitorIgHandle) {
        return repository
                .findByOwnerIgIdAndCompetitorIgHandle(ownerIgId, competitorIgHandle)
                .map(this::toDomain);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<MonitoredProfile> findAllActiveByCompetitorHandle(String competitorIgHandle) {
        return repository.findAllByCompetitorIgHandleAndActiveTrue(competitorIgHandle)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void updateLastCollectedAt(UUID id, Instant lastCollectedAt) {
        repository.findById(id).ifPresent(entity -> {
            entity.setLastCollectedAt(lastCollectedAt);
            repository.save(entity);
        });
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private MonitoredProfileEntity toEntity(MonitoredProfile domain) {
        MonitoredProfileEntity entity = new MonitoredProfileEntity();
        entity.setId(domain.getId());
        entity.setOwnerIgId(domain.getOwnerIgId());
        entity.setCompetitorIgHandle(domain.getCompetitorIgHandle());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastCollectedAt(domain.getLastCollectedAt());
        return entity;
    }

    private MonitoredProfile toDomain(MonitoredProfileEntity entity) {
        return MonitoredProfile.builder()
                .id(entity.getId())
                .ownerIgId(entity.getOwnerIgId())
                .competitorIgHandle(entity.getCompetitorIgHandle())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .lastCollectedAt(entity.getLastCollectedAt())
                .build();
    }
}