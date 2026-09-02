package pl.autopilot.datacollector.infrastructure.instagram;

import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramOAuthClient;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramUserResponse;
import pl.autopilot.datacollector.infrastructure.keycloak.KeycloakBrokerClient;
import pl.autopilot.datacollector.infrastructure.persistence.entity.AccessTokenEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.AccessTokenJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LinkInstagramAccountServiceTest {

    @Mock
    private KeycloakBrokerClient keycloakBrokerClient;

    @Mock
    private InstagramOAuthClient instagramOAuthClient;

    @Mock
    private AccessTokenJpaRepository accessTokenJpaRepository;

    @InjectMocks
    private LinkInstagramAccountService service;

    private static final String OWNER_ID = "keycloak-sub-123";
    private static final String KEYCLOAK_JWT = "keycloak-jwt-token";
    private static final String FACEBOOK_TOKEN = "fb-token-abc";
    private static final String OWNER_IG_ID = "ig-business-456";
    private static final String OWNER_USERNAME = "fotograf_jan";

    private AccessToken longLivedToken() {
        return AccessToken.builder()
                .token("long-lived-xyz")
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(5_184_000))
                .build();
    }

    private InstagramUserResponse meResponse() {
        InstagramUserResponse response = new InstagramUserResponse();
        // ustawiamy przez refleksję lub budowniczego zależnie od struktury klasy
        // zakładam że InstagramUserResponse ma setter lub działa przez Jackson
        InstagramUserResponse.IgAccount igAccount = new InstagramUserResponse.IgAccount();
        igAccount.setId(OWNER_IG_ID);
        igAccount.setUsername(OWNER_USERNAME);

        InstagramUserResponse.PageData pageData = new InstagramUserResponse.PageData();
        pageData.setInstagramBusinessAccount(igAccount);

        InstagramUserResponse.AccountList accountList = new InstagramUserResponse.AccountList();
        accountList.setData(List.of(pageData));

        response.setAccounts(accountList);
        return response;
    }

    @Test
    void link_shouldSaveNewEntity_whenAccountNotLinkedBefore() {
        // given
        given(keycloakBrokerClient.getFacebookToken(KEYCLOAK_JWT)).willReturn(FACEBOOK_TOKEN);
        given(instagramOAuthClient.exchangeForLongLivedToken(FACEBOOK_TOKEN)).willReturn(longLivedToken());
        given(instagramOAuthClient.fetchMe("long-lived-xyz")).willReturn(meResponse());
        given(accessTokenJpaRepository.findByOwnerIgId(OWNER_IG_ID)).willReturn(Optional.empty());

        // when
        service.link(OWNER_ID, KEYCLOAK_JWT);

        // then
        ArgumentCaptor<AccessTokenEntity> captor = ArgumentCaptor.forClass(AccessTokenEntity.class);
        then(accessTokenJpaRepository).should().save(captor.capture());

        AccessTokenEntity saved = captor.getValue();
        assertThat(saved.getOwnerIgId()).isEqualTo(OWNER_IG_ID);
        assertThat(saved.getOwnerUsername()).isEqualTo(OWNER_USERNAME);
        assertThat(saved.getToken()).isEqualTo("long-lived-xyz");
        assertThat(saved.getTokenType()).isEqualTo(AccessToken.TokenType.LONG_LIVED.name());
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void link_shouldUpdateExistingEntity_whenAccountAlreadyLinked() {
        // given
        UUID existingId = UUID.randomUUID();
        Instant existingCreatedAt = Instant.now().minusSeconds(3600);

        AccessTokenEntity existing = new AccessTokenEntity();
        existing.setId(existingId);
        existing.setCreatedAt(existingCreatedAt);
        existing.setOwnerIgId(OWNER_IG_ID);

        given(keycloakBrokerClient.getFacebookToken(KEYCLOAK_JWT)).willReturn(FACEBOOK_TOKEN);
        given(instagramOAuthClient.exchangeForLongLivedToken(FACEBOOK_TOKEN)).willReturn(longLivedToken());
        given(instagramOAuthClient.fetchMe("long-lived-xyz")).willReturn(meResponse());
        given(accessTokenJpaRepository.findByOwnerIgId(OWNER_IG_ID)).willReturn(Optional.of(existing));

        // when
        service.link(OWNER_ID, KEYCLOAK_JWT);

        // then
        ArgumentCaptor<AccessTokenEntity> captor = ArgumentCaptor.forClass(AccessTokenEntity.class);
        then(accessTokenJpaRepository).should().save(captor.capture());

        AccessTokenEntity saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo("long-lived-xyz");
        assertThat(saved.getOwnerIgId()).isEqualTo(OWNER_IG_ID);
    }

    @Test
    void link_shouldThrow_whenNoInstagramBusinessAccount() {
        // given
        InstagramUserResponse emptyResponse = new InstagramUserResponse();

        given(keycloakBrokerClient.getFacebookToken(KEYCLOAK_JWT)).willReturn(FACEBOOK_TOKEN);
        given(instagramOAuthClient.exchangeForLongLivedToken(FACEBOOK_TOKEN)).willReturn(longLivedToken());
        given(instagramOAuthClient.fetchMe("long-lived-xyz")).willReturn(emptyResponse);

        // when / then
        assertThatThrownBy(() -> service.link(OWNER_ID, KEYCLOAK_JWT))
                .isInstanceOf(InstagramBusinessAccountNotFoundException.class)
                .hasMessageContaining(OWNER_ID);

        then(accessTokenJpaRepository).should(never()).save(any());
    }
}