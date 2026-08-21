package pl.autopilot.competitoragent.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.PostingHourStatsEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostingHourStatsJpaRepository
        extends JpaRepository<PostingHourStatsEntity, UUID> {

    Optional<PostingHourStatsEntity> findByCompetitorUsernameAndMediaTypeAndHourOfDay(
            String competitorUsername, String mediaType, short hourOfDay);

    List<PostingHourStatsEntity> findByCompetitorUsername(String competitorUsername);

    List<PostingHourStatsEntity> findByCompetitorUsernameAndMediaType(
            String competitorUsername, String mediaType);
}