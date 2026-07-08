package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.PostNicheRelevanceEntity;

import java.util.Optional;
import java.util.UUID;

public interface PostNicheRelevanceJpaRepository
        extends JpaRepository<PostNicheRelevanceEntity, UUID> {

    Optional<PostNicheRelevanceEntity> findByIgMediaIdAndSourceTypeAndOwnerIgId(
            String igMediaId, String sourceType, String ownerIgId);
}