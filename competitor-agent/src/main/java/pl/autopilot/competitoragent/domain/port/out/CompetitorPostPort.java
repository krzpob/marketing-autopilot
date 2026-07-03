package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.CompetitorPost;

import java.util.List;
import java.util.Optional;

public interface CompetitorPostPort {

    void save(CompetitorPost post);

    Optional<CompetitorPost> findByIgMediaId(String igMediaId);

    boolean existsByIgMediaId(String igMediaId);

    /** Ostatnie N postów konkurenta — do liczenia średniej kroczącej */
    List<CompetitorPost> findLatestByUsername(String competitorUsername, int limit);
}
