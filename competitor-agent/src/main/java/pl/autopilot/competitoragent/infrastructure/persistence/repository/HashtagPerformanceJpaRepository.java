package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.HashtagPerformanceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface HashtagPerformanceJpaRepository extends JpaRepository<HashtagPerformanceEntity, UUID> {
    Optional<HashtagPerformanceEntity> findTopByHashtagOrderByCollectedAtDesc(String hashtag);
    List<HashtagPerformanceEntity> findByHashtagOrderByCollectedAtDesc(String hashtag);
}