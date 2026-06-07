package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.assertj.core.api.BDDSoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramErrorResponse;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SoftAssertionsExtension.class)
class InstagramApiExceptionTest {

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    @Test
    void shouldFormatMessageWithAllErrorDetails() {
        // given
        InstagramErrorResponse.ErrorDetail detail = anErrorDetail(190, "Token expired", "OAuthException", "abc123");

        // when
        InstagramApiException ex = new InstagramApiException(detail);

        // then
        softly.then(ex.getMessage()).contains("190");
        softly.then(ex.getMessage()).contains("Token expired");
        softly.then(ex.getMessage()).contains("OAuthException");
        softly.then(ex.getMessage()).contains("abc123");
        softly.then(ex.getCode()).isEqualTo(190);
        softly.then(ex.getType()).isEqualTo("OAuthException");
        softly.then(ex.getFbtraceId()).isEqualTo("abc123");
    }

    @Test
    void shouldDetectExpiredToken() {
        // given
        InstagramApiException ex = exceptionWithCode(190);

        // then
        then(ex.isTokenExpired()).isTrue();
        then(ex.isRateLimited()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 17})
    void shouldDetectRateLimit(int code) {
        // given
        InstagramApiException ex = exceptionWithCode(code);

        // then
        then(ex.isRateLimited()).isTrue();
        then(ex.isTokenExpired()).isFalse();
    }

    @Test
    void shouldNotDetectTokenExpiredForOtherCodes() {
        then(exceptionWithCode(100).isTokenExpired()).isFalse();
        then(exceptionWithCode(200).isTokenExpired()).isFalse();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private InstagramApiException exceptionWithCode(int code) {
        return new InstagramApiException(anErrorDetail(code, "Error", "GraphMethodException", "trace"));
    }

    private InstagramErrorResponse.ErrorDetail anErrorDetail(int code, String message,
                                                              String type, String fbtrace) {
        InstagramErrorResponse.ErrorDetail detail = new InstagramErrorResponse.ErrorDetail();
        detail.setCode(code);
        detail.setMessage(message);
        detail.setType(type);
        detail.setFbtraceId(fbtrace);
        return detail;
    }
}