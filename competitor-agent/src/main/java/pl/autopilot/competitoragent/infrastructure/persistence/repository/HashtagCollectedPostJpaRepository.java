package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.HashtagCollectedPostEntity;

import java.util.Optional;
import java.util.UUID;

public interface HashtagCollectedPostJpaRepository
        extends JpaRepository<HashtagCollectedPostEntity, UUID> {

    boolean existsByIgMediaIdAndHashtag(String igMediaId, String hashtag);

    Optional<HashtagCollectedPostEntity> findByIgMediaIdAndHashtag(
            String igMediaId, String hashtag);
}