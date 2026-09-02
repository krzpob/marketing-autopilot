package pl.autopilot.datacollector.infrastructure.web;


import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import pl.autopilot.datacollector.domain.port.out.AccessTokenPort;
import pl.autopilot.datacollector.infrastructure.config.SecurityConfig;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {SecurityConfig.class, MetaCallbackController.class})
@ExtendWith({SoftAssertionsExtension.class, SpringExtension.class})
class MetaCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessTokenPort accessTokenPort;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void debugSecurity() throws Exception {
        System.out.println("Filter chain: " + securityFilterChain);
    }

    @Test
    void shouldReturn200WithConfirmationCodeAndUrl() throws Exception {
        // when
        MvcResult result = mockMvc.perform(post("/meta/data-deletion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"user_id":"12345678","signed_request":"abc.def"}
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        Map<String, Object> body = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {});

        softly.then(body).containsKey("confirmation_code");
        softly.then(body).containsKey("url");
        softly.then((String) body.get("confirmation_code"))
                .startsWith("autopilot-deletion-");
        softly.then((String) body.get("url"))
                .contains("krzpob.github.io");
    }

    @Test
    void shouldAcceptRequestWithoutBody() throws Exception {
        mockMvc.perform(post("/meta/data-deletion"))
                .andExpect(status().isOk());
    }
}