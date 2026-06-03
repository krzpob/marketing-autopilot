package pl.autopilot.datacollector.infrastructure.web;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetaCallbackController.class)
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