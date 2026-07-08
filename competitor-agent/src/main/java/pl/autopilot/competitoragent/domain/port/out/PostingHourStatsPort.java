package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.PostingHourStats;

import java.util.List;
import java.util.Optional;

public interface PostingHourStatsPort {

    void save(PostingHourStats stats);

    Optional<PostingHourStats> findByUsernameAndMediaTypeAndHour(
            String competitorUsername, String mediaType, short hourOfDay);

    List<PostingHourStats> findByUsername(String competitorUsername);

    List<PostingHourStats> findByUsernameAndMediaType(
            String competitorUsername, String mediaType);
}