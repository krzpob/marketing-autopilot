package pl.autopilot.datacollector.infrastructure.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramOAuthClient;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private AccessTokenPort      accessTokenPort;
    @Mock
    private InstagramOAuthClient oAuthClient;

    @InjectMocks
    private RefreshTokenService service;

    // ── refreshAllExpiring ────────────────────────────────────────────────────

    @Test
    void shouldRefreshAllExpiringTokens() {
        // given
        AccessToken token1 = aExpiringToken("owner1");
        AccessToken token2 = aExpiringToken("owner2");

        given(accessTokenPort.findAllExpiringSoon())
                .willReturn(List.of(token1, token2));
        given(oAuthClient.refreshLongLivedToken(token1))
                .willReturn(aRefreshedToken(token1));
        given(oAuthClient.refreshLongLivedToken(token2))
                .willReturn(aRefreshedToken(token2));

        // when
        service.refreshAllExpiring();

        // then
        BDDMockito.then(oAuthClient).should().refreshLongLivedToken(token1);
        BDDMockito.then(oAuthClient).should().refreshLongLivedToken(token2);
        BDDMockito.then(accessTokenPort).should(BDDMockito.times(2)).save(any());
    }

    @Test
    void shouldDoNothingWhenNoExpiringTokens() {
        // given
        given(accessTokenPort.findAllExpiringSoon()).willReturn(List.of());

        // when
        service.refreshAllExpiring();

        // then
        BDDMockito.then(oAuthClient).shouldHaveNoInteractions();
        BDDMockito.then(accessTokenPort).should().findAllExpiringSoon();
        BDDMockito.then(accessTokenPort).shouldHaveNoMoreInteractions();
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    void shouldSaveRefreshedToken() {
        // given
        AccessToken token    = aExpiringToken("owner1");
        AccessToken refreshed = aRefreshedToken(token);

        given(accessTokenPort.findAllExpiringSoon()).willReturn(List.of(token));
        given(oAuthClient.refreshLongLivedToken(token)).willReturn(refreshed);

        // when
        service.refreshAllExpiring();

        // then
        BDDMockito.then(accessTokenPort).should().save(refreshed);
    }

    @Test
    void shouldSkipExpiredToken() {
        // given
        AccessToken expired = aExpiredToken("owner1");

        given(accessTokenPort.findAllExpiringSoon()).willReturn(List.of(expired));

        // when
        service.refreshAllExpiring();

        // then — token wygasł, nie próbujemy odświeżyć
        BDDMockito.then(oAuthClient).shouldHaveNoInteractions();
        BDDMockito.then(accessTokenPort).should(BDDMockito.never()).save(any());
    }

    @Test
    void shouldContinueRefreshingWhenOneTokenFails() {
        // given
        AccessToken token1 = aExpiringToken("owner1");
        AccessToken token2 = aExpiringToken("owner2");

        given(accessTokenPort.findAllExpiringSoon())
                .willReturn(List.of(token1, token2));
        given(oAuthClient.refreshLongLivedToken(token1))
                .willThrow(new RuntimeException("Instagram API error"));
        given(oAuthClient.refreshLongLivedToken(token2))
                .willReturn(aRefreshedToken(token2));

        // when — nie rzuca wyjątku
        service.refreshAllExpiring();

        // then — token2 odświeżony mimo błędu token1
        BDDMockito.then(accessTokenPort).should().save(any());
        BDDMockito.then(oAuthClient).should().refreshLongLivedToken(token1);
        BDDMockito.then(oAuthClient).should().refreshLongLivedToken(token2);
    }

    @Test
    void shouldNotSaveWhenRefreshThrows() {
        // given
        AccessToken token = aExpiringToken("owner1");

        given(accessTokenPort.findAllExpiringSoon()).willReturn(List.of(token));
        given(oAuthClient.refreshLongLivedToken(token))
                .willThrow(new RuntimeException("Instagram API error"));

        // when — nie rzuca wyjątku
        service.refreshAllExpiring();

        // then
        BDDMockito.then(accessTokenPort).should(BDDMockito.never()).save(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AccessToken aExpiringToken(String ownerIgId) {
        return AccessToken.builder()
                .ownerIgId(ownerIgId)
                .ownerUsername(ownerIgId)
                .token("token-" + ownerIgId)
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(3 * 24 * 3600)) // wygasa za 3 dni
                .build();
    }

    private AccessToken aExpiredToken(String ownerIgId) {
        return AccessToken.builder()
                .ownerIgId(ownerIgId)
                .ownerUsername(ownerIgId)
                .token("token-" + ownerIgId)
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().minusSeconds(3600)) // wygasł godzinę temu
                .build();
    }

    private AccessToken aRefreshedToken(AccessToken original) {
        return original.toBuilder()
                .token("refreshed-" + original.getOwnerIgId())
                .expiresAt(Instant.now().plusSeconds(60L * 24 * 3600))
                .refreshedAt(Instant.now())
                .build();
    }
}