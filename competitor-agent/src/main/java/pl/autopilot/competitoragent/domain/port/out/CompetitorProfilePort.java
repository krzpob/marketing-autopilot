package pl.autopilot.competitoragent.domain.port.out;

import pl.autopilot.competitoragent.domain.model.CompetitorProfile;

import java.util.Optional;

public interface CompetitorProfilePort {

    void save(CompetitorProfile profile);

    Optional<CompetitorProfile> findByUsername(String username);
}