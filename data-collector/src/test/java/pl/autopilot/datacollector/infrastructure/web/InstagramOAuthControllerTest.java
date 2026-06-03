package pl.autopilot.datacollector.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.infrastructure.instagram.client.InstagramOAuthClient;
import org.hamcrest.Matchers;

import java.time.Instant;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstagramOAuthController.class)
class InstagramOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstagramOAuthClient oAuthClient;

    @MockitoBean
    private AccessTokenPort accessTokenPort;

    // ── GET /oauth/instagram/authorize ───────────────────────────────────────

    @Test
    void shouldRedirectToInstagramAuthorizationUrl() throws Exception {
        // given
        given(oAuthClient.buildAuthorizationUrl())
                .willReturn("https://www.facebook.com/dialog/oauth?client_id=123&response_type=code");

        // when / then
        mockMvc.perform(get("/oauth/instagram/authorize"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://www.facebook.com/**"));
    }

    // ── GET /oauth/instagram/callback ────────────────────────────────────────

    @Test
    void shouldExchangeCodeSaveTokenAndReturn200() throws Exception {
        // given
        AccessToken shortLived = aShortLivedToken();
        AccessToken longLived  = aLongLivedToken();

        given(oAuthClient.exchangeCodeForShortLivedToken("auth-code-abc"))
                .willReturn(shortLived);
        given(oAuthClient.exchangeForLongLivedToken(shortLived))
                .willReturn(longLived);

        // when / then
        mockMvc.perform(get("/oauth/instagram/callback")
                        .param("code", "auth-code-abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Token ważny do")));

        BDDMockito.then(accessTokenPort).should().save(longLived);
    }

    @Test
    void shouldReturn400WhenBothCodeAndErrorAreMissing() throws Exception {
        // when / then
        mockMvc.perform(get("/oauth/instagram/callback"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("Brak kodu")));

        BDDMockito.then(oAuthClient).shouldHaveNoInteractions();
    }

    @Test
    void shouldReturn400WhenOAuthErrorReceived() throws Exception {
        // when / then
        mockMvc.perform(get("/oauth/instagram/callback")
                        .param("error",             "access_denied")
                        .param("error_description", "User denied access"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("access_denied")));

        BDDMockito.then(oAuthClient).shouldHaveNoInteractions();
    }

    @Test
    void shouldReturn500WhenTokenExchangeFails() throws Exception {
        // given
        given(oAuthClient.exchangeCodeForShortLivedToken(any()))
                .willThrow(new RuntimeException("Instagram API unavailable"));

        // when / then
        mockMvc.perform(get("/oauth/instagram/callback")
                        .param("code", "some-code"))
                .andExpect(status().isInternalServerError());

        BDDMockito.then(accessTokenPort).shouldHaveNoInteractions();
    }

    // ── factory helpers ──────────────────────────────────────────────────────

    private AccessToken aShortLivedToken() {
        return AccessToken.builder()
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .token("short-lived-token")
                .tokenType(AccessToken.TokenType.SHORT_LIVED)
                .expiresAt(Instant.now().plusSeconds(3_600))
                .build();
    }

    private AccessToken aLongLivedToken() {
        return AccessToken.builder()
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .token("long-lived-token")
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(60L * 24 * 3_600))
                .build();
    }
}