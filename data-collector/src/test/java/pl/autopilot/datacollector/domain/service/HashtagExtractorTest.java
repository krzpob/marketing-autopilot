package pl.autopilot.datacollector.domain.service;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SoftAssertionsExtension.class)
class HashtagExtractorTest {

    private final HashtagExtractor extractor = new HashtagExtractor();

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── extractHashtags ──────────────────────────────────────────────────────

    @Test
    void shouldExtractHashtagsFromCaption() {
        // when
        List<String> result = extractor.extractHashtags(
                "Piękne zdjęcie #fotografia #portret #warszawa");

        // then
        then(result).containsExactlyInAnyOrder("fotografia", "portret", "warszawa");
    }

    @Test
    void shouldReturnHashtagsInLowercase() {
        // when
        List<String> result = extractor.extractHashtags("#Fotografia #PORTRET");

        // then
        then(result).containsExactlyInAnyOrder("fotografia", "portret");
    }

    @Test
    void shouldDeduplicateHashtags() {
        // when
        List<String> result = extractor.extractHashtags(
                "#fotografia #fotografia #portret");

        // then
        then(result).containsExactlyInAnyOrder("fotografia", "portret");
        then(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoCaptionHashtags() {
        then(extractor.extractHashtags("Zwykły tekst bez tagów")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForNullCaption() {
        then(extractor.extractHashtags(null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForBlankCaption() {
        then(extractor.extractHashtags("   ")).isEmpty();
    }

    // ── extractMentions ──────────────────────────────────────────────────────

    @Test
    void shouldExtractMentionsFromCaption() {
        // when
        List<String> result = extractor.extractMentions(
                "Zdjęcie z @jan_kowalski i @anna_nowak");

        // then
        then(result).containsExactlyInAnyOrder("jan_kowalski", "anna_nowak");
    }

    @Test
    void shouldReturnMentionsInLowercase() {
        // when
        List<String> result = extractor.extractMentions("@JanKowalski @ANNA");

        // then
        then(result).containsExactlyInAnyOrder("jankowalski", "anna");
    }

    @Test
    void shouldDeduplicateMentions() {
        // when
        List<String> result = extractor.extractMentions(
                "@jan @jan @anna");

        // then
        then(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoMentions() {
        then(extractor.extractMentions("Tekst bez wzmianek")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForNullMentionCaption() {
        then(extractor.extractMentions(null)).isEmpty();
    }

    // ── caption mieszana ─────────────────────────────────────────────────────

    @Test
    void shouldExtractHashtagsAndMentionsSeparately() {
        // given
        String caption = "Sesja z @modelka_ania #fotografia #portret @studio_xyz";

        // when / then
        softly.then(extractor.extractHashtags(caption))
                .containsExactlyInAnyOrder("fotografia", "portret");
        softly.then(extractor.extractMentions(caption))
                .containsExactlyInAnyOrder("modelka_ania", "studio_xyz");
        softly.assertAll();
    }

    @Test
    void shouldHandleCaptionWithOnlyEmoji() {
        softly.then(extractor.extractHashtags("🌟✨📸")).isEmpty();
        softly.then(extractor.extractMentions("🌟✨📸")).isEmpty();
        softly.assertAll();
    }
}