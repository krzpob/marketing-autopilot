package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.infrastructure.instagram.mapper.InstagramMediaMapper;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramErrorResponse;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.time.Instant;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
abstract class InstagramApiClientTestBase {

    static final String OWNER_IG_ID = "12345678";
    static final String OWNER_TOKEN = "long-lived-token";
    static final String GRAPH_BASE  = "https://graph.facebook.com/v19.0";

    @Mock
    InstagramGraphClient   graphClient;
    @Mock
    InstagramApiProperties properties;
    @Mock
    InstagramMediaMapper   mediaMapper;

    void givenGraphBaseUrl() {
        given(properties.getGraphBaseUrl()).willReturn(GRAPH_BASE);
    }

    AccessToken aValidToken() {
        return AccessToken.builder()
                .ownerIgId(OWNER_IG_ID)
                .ownerUsername("testuser")
                .token(OWNER_TOKEN)
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(60L * 24 * 3600))
                .build();
    }

    InstagramMediaResponse.MediaItem aMediaItem(String shortcode) {
        InstagramMediaResponse.MediaItem item = new InstagramMediaResponse.MediaItem();
        item.setShortcode(shortcode);
        item.setMediaType("IMAGE");
        item.setTimestamp("2024-01-01T00:00:00+0000");
        return item;
    }

    InstagramMediaResponse.MediaItem aMediaItem(String shortcode, String timestamp) {
        InstagramMediaResponse.MediaItem item = new InstagramMediaResponse.MediaItem();
        item.setShortcode(shortcode);
        item.setMediaType("IMAGE");
        item.setTimestamp(timestamp);
        return item;
    }

    CollectedPost aPost(String shortcode) {
        return CollectedPost.builder()
                .shortcode(shortcode)
                .ownerIgId(OWNER_IG_ID)
                .ownerUsername("testuser")
                .mediaType(CollectedPost.MediaType.IMAGE)
                .publishedAt(Instant.now())
                .build();
    }

    InstagramErrorResponse.ErrorDetail anErrorDetail(int code, String message) {
        InstagramErrorResponse.ErrorDetail detail =
                new InstagramErrorResponse.ErrorDetail();
        detail.setCode(code);
        detail.setMessage(message);
        detail.setType("OAuthException");
        detail.setFbtraceId("trace");
        return detail;
    }
}