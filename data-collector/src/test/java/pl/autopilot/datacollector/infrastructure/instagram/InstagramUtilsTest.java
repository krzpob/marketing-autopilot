package pl.autopilot.datacollector.infrastructure.instagram;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.BDDAssertions.then;

class InstagramUtilsTest {

    @Test
    void shouldParseTimestampWithoutColonInOffset() {
        // given
        String timestamp = "2024-06-15T12:00:00+0000";

        // when
        Instant result = InstagramUtils.parseTimestamp(timestamp);

        // then
        then(result.toString()).startsWith("2024-06-15T12:00:00");
    }

    @Test
    void shouldParseTimestampWithPositiveOffset() {
        // given
        String timestamp = "2024-06-15T14:00:00+0200";

        // when
        Instant result = InstagramUtils.parseTimestamp(timestamp);

        // then — UTC: 14:00 - 2h = 12:00
        then(result.toString()).startsWith("2024-06-15T12:00:00");
    }

    @Test
    void shouldReturnNonNullInstantForNullTimestamp() {
        // when
        Instant result = InstagramUtils.parseTimestamp(null);

        // then
        then(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-a-date", "2024-06-15", "15/06/2024 12:00"})
    void shouldReturnNonNullInstantForInvalidTimestamp(String invalid) {
        // when
        Instant result = InstagramUtils.parseTimestamp(invalid);

        // then
        then(result).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.instagram.com/p/ABC123/,        ABC123",
            "https://www.instagram.com/reel/XYZ789/,     XYZ789",
            "https://www.instagram.com/p/ABC123,          ABC123",
    })
    void shouldExtractShortcodeFromPermalink(String permalink, String expected) {
        then(InstagramUtils.extractShortcode(permalink.trim())).isEqualTo(expected);
    }

    @Test
    void shouldReturnNullForNullPermalink() {
        then(InstagramUtils.extractShortcode(null)).isNull();
    }
}