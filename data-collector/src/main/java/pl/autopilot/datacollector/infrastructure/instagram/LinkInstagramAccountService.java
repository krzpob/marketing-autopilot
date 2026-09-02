package pl.autopilot.datacollector.infrastructure.instagram;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramOAuthClient;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramUserResponse;
import pl.autopilot.datacollector.infrastructure.keycloak.KeycloakBrokerClient;
import pl.autopilot.datacollector.infrastructure.persistence.entity.AccessTokenEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.AccessTokenJpaRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkInstagramAccountService {

    private final KeycloakBrokerClient keycloakBrokerClient;
    private final InstagramOAuthClient instagramOAuthClient;
    private final AccessTokenJpaRepository accessTokenJpaRepository;

    public void link(String ownerId, String keycloakJwt) {
        String facebookToken = keycloakBrokerClient.getFacebookToken(keycloakJwt);

        AccessToken longLived = instagramOAuthClient.exchangeForLongLivedToken(facebookToken);

        InstagramUserResponse me = instagramOAuthClient.fetchMe(longLived.getToken());
        String ownerIgId = me.getInstagramAccountId();
        String ownerUsername = me.getInstagramUsername();

        if (ownerIgId == null) {
            throw new InstagramBusinessAccountNotFoundException(ownerId);
        }

        AccessTokenEntity entity = accessTokenJpaRepository.findByOwnerIgId(ownerIgId)
                .orElseGet(AccessTokenEntity::new);

        entity.setId(UUID.randomUUID());
        entity.setOwnerIgId(ownerIgId);
        entity.setOwnerUsername(ownerUsername);
        entity.setToken(longLived.getToken());
        entity.setTokenType(AccessToken.TokenType.LONG_LIVED.name());
        entity.setExpiresAt(longLived.getExpiresAt());
        entity.setCreatedAt(Instant.now());

        accessTokenJpaRepository.save(entity);
        log.info("Konto IG połączone: ownerId={}, ownerIgId={}, username={}",
                ownerId, ownerIgId, ownerUsername);
    }
}