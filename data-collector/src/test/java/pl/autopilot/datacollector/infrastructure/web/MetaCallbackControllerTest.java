package pl.autopilot.datacollector.infrastructure.web;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.With;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MetaCallbackController.class,
    excludeAutoConfiguration = {
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    })
@ExtendWith(SoftAssertionsExtension.class)
class MetaCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturn200WithConfirmationCodeAndUrl() throws Exception {
        // when
        MvcResult result = mockMvc.perform(post("/meta/data-deletion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"user_id":"12345678","signed_request":"abc.def"}
                                """))
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