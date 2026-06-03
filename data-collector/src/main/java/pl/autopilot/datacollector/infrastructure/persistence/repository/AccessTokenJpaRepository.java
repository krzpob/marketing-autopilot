package pl.autopilot.datacollector.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.datacollector.infrastructure.persistence.entity.AccessTokenEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenJpaRepository extends JpaRepository<AccessTokenEntity, UUID> {

    Optional<AccessTokenEntity> findByOwnerIgId(String ownerIgId);

    /** Tokeny gdzie expiresAt < threshold — czyli wygasną przed podanym momentem */
    List<AccessTokenEntity> findAllByExpiresAtBefore(Instant threshold);

    void deleteByOwnerIgId(String ownerIgId);
}