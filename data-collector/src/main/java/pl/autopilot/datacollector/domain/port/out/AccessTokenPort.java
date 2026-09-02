package pl.autopilot.datacollector.domain.port.out;

import java.util.List;
import java.util.Optional;

import pl.autopilot.datacollector.domain.model.AccessToken;

public interface AccessTokenPort {
    void save(AccessToken token);

    Optional<AccessToken> findByOwnerIgId(String ownerIgId);

    List<AccessToken> findAll();

    /** Tokeny wygasające w ciągu 7 dni */
    List<AccessToken> findAllExpiringSoon();

    void delete(String ownerIgId);

}