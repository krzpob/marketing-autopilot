package pl.autopilot.datacollector.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.infrastructure.persistence.entity.AccessTokenEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.AccessTokenJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccessTokenAdapter implements AccessTokenPort {

    private final AccessTokenJpaRepository repository;

    @Override
    public void save(AccessToken token) {
        repository.save(toEntity(token));
    }

    @Override
    public Optional<AccessToken> findByOwnerIgId(String ownerIgId) {
        return repository.findByOwnerIgId(ownerIgId)
                .map(this::toDomain);
    }

    @Override
    public List<AccessToken> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AccessToken> findAllExpiringSoon() {
        Instant threshold = Instant.now().plusSeconds(7L * 24 * 3600);
        return repository.findAllByExpiresAtBefore(threshold)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(String ownerIgId) {
        repository.deleteByOwnerIgId(ownerIgId);
    }

    // ── mappery ──────────────────────────────────────────────────────────────

    private AccessTokenEntity toEntity(AccessToken domain) {
        AccessTokenEntity entity = new AccessTokenEntity();
        entity.setId(domain.getId());
        entity.setOwnerIgId(domain.getOwnerIgId());
        entity.setOwnerUsername(domain.getOwnerUsername());
        entity.setToken(domain.getToken());
        entity.setTokenType(domain.getTokenType().name());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setRefreshedAt(domain.getRefreshedAt());
        return entity;
    }

    private AccessToken toDomain(AccessTokenEntity entity) {
        return AccessToken.builder()
                .id(entity.getId())
                .ownerIgId(entity.getOwnerIgId())
                .ownerUsername(entity.getOwnerUsername())
                .token(entity.getToken())
                .tokenType(AccessToken.TokenType.valueOf(entity.getTokenType()))
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .refreshedAt(entity.getRefreshedAt())
                .build();
    }
}