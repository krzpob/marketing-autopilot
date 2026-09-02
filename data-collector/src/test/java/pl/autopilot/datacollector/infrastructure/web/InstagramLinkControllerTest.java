package pl.autopilot.datacollector.infrastructure.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;

import pl.autopilot.datacollector.infrastructure.config.SecurityConfig;
import pl.autopilot.datacollector.infrastructure.instagram.LinkInstagramAccountService;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InstagramLinkController.class)
@Import(SecurityConfig.class)
class InstagramLinkControllerTest {

    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkInstagramAccountService linkInstagramAccountService;

    @MockitoBean
    private JwtDecoder jwtDecoder;


    @Test
    @WithMockUser
    void link_shouldReturn200_whenJwtValid() throws Exception {
        // given
        Jwt jwt = Jwt.withTokenValue("valid-jwt-token")
                .header("alg", "RS256")
                .subject("keycloak-sub-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        given(jwtDecoder.decode("valid-jwt-token")).willReturn(jwt);
        willDoNothing().given(linkInstagramAccountService).link(any(), any());

        // when / then
        mockMvc.perform(post("/oauth/instagram/link")
                        .header("Authorization", "Bearer valid-jwt-token"))
                .andExpect(status().isOk());

        then(linkInstagramAccountService).should().link(any(), eq("valid-jwt-token"));
    }

    @Test
    void link_shouldReturn401_whenNoAuthentication() throws Exception {
        mockMvc.perform(post("/oauth/instagram/link"))
                .andExpect(status().isUnauthorized());
    }
}
