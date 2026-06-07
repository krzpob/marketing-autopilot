package pl.autopilot.datacollector.infrastructure.instagram.mapper;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.service.HashtagExtractor;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class InstagramMediaMapperTest {

    @Mock
    private HashtagExtractor hashtagExtractor;

    @InjectMocks
    private InstagramMediaMapper mapper;

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    @Test
    void shouldMapAllFieldsFromMediaItem() {
        // given
        InstagramMediaResponse.MediaItem item = aMediaItem(
                "abc123", "IMAGE", "Piękne zdjęcie #fotografia @jan",
                "https://media.url", "https://insta.com/p/abc123",
                42L, 7, "2024-03-15T10:30:00+0000"
        );

        given(hashtagExtractor.extractHashtags("Piękne zdjęcie #fotografia @jan"))
                .willReturn(List.of("fotografia"));
        given(hashtagExtractor.extractMentions("Piękne zdjęcie #fotografia @jan"))
                .willReturn(List.of("jan"));

        // when
        CollectedPost post = mapper.toDomain(item, "12345678", "testuser");

        // then
        softly.then(post.getShortcode()).isEqualTo("abc123");
        softly.then(post.getOwnerIgId()).isEqualTo("12345678");
        softly.then(post.getOwnerUsername()).isEqualTo("testuser");
        softly.then(post.getMediaType()).isEqualTo(CollectedPost.MediaType.IMAGE);
        softly.then(post.getCaption()).isEqualTo("Piękne zdjęcie #fotografia @jan");
        softly.then(post.getHashtags()).containsExactly("fotografia");
        softly.then(post.getMentions()).containsExactly("jan");
        softly.then(post.getMediaUrl()).isEqualTo("https://media.url");
        softly.then(post.getPermalink()).isEqualTo("https://insta.com/p/abc123");
        softly.then(post.getLikeCount()).isEqualTo(42L);
        softly.then(post.getCommentsCount()).isEqualTo(7);
        softly.then(post.getPublishedAt()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "IMAGE,          IMAGE",
            "VIDEO,          VIDEO",
            "CAROUSEL_ALBUM, CAROUSEL_ALBUM",
            "REELS,          REEL",
            "UNKNOWN_TYPE,   UNKNOWN",
            ","             + "UNKNOWN"
    })
    void shouldMapMediaTypes(String raw, CollectedPost.MediaType expected) {
        // given
        InstagramMediaResponse.MediaItem item = aMediaItem(
                "x", raw, null, null, null, 0L, 0, "2024-01-01T00:00:00+0000");
        given(hashtagExtractor.extractHashtags(null)).willReturn(List.of());
        given(hashtagExtractor.extractMentions(null)).willReturn(List.of());

        // when
        CollectedPost post = mapper.toDomain(item, "id", "user");

        // then
        then(post.getMediaType()).isEqualTo(expected);
    }

    @Test
    void shouldParseIso8601Timestamp() {
        // given
        InstagramMediaResponse.MediaItem item = aMediaItem(
                "x", "IMAGE", null, null, null, 0L, 0, "2024-06-15T12:00:00+0000");
        given(hashtagExtractor.extractHashtags(null)).willReturn(List.of());
        given(hashtagExtractor.extractMentions(null)).willReturn(List.of());

        // when
        CollectedPost post = mapper.toDomain(item, "id", "user");

        // then
        then(post.getPublishedAt()).isNotNull();
        then(post.getPublishedAt().toString()).startsWith("2024-06-15");
    }

    @Test
    void shouldHandleNullTimestamp() {
        // given
        InstagramMediaResponse.MediaItem item = aMediaItem(
                "x", "IMAGE", null, null, null, 0L, 0, null);
        given(hashtagExtractor.extractHashtags(null)).willReturn(List.of());
        given(hashtagExtractor.extractMentions(null)).willReturn(List.of());

        // when
        CollectedPost post = mapper.toDomain(item, "id", "user");

        // then — fallback to Instant.now()
        then(post.getPublishedAt()).isNotNull();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private InstagramMediaResponse.MediaItem aMediaItem(String shortcode, String mediaType,
            String caption, String mediaUrl, String permalink,
            long likeCount, int commentsCount, String timestamp) {

        InstagramMediaResponse.MediaItem item = new InstagramMediaResponse.MediaItem();
        item.setShortcode(shortcode);
        item.setMediaType(mediaType);
        item.setCaption(caption);
        item.setMediaUrl(mediaUrl);
        item.setPermalink(permalink);
        item.setLikeCount(likeCount);
        item.setCommentsCount(commentsCount);
        item.setTimestamp(timestamp);
        return item;
    }
}