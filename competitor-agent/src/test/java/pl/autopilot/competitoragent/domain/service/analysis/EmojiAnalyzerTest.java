package pl.autopilot.competitoragent.domain.service.analysis;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(SoftAssertionsExtension.class)
class EmojiAnalyzerTest {

    private final EmojiAnalyzer analyzer = new EmojiAnalyzer();

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── null / empty ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyStatsForNullCaption() {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(null);
        softly.then(stats.totalEmojiCount()).isZero();
        softly.then(stats.emojiDensity()).isZero();
    }

    @Test
    void shouldReturnEmptyStatsForEmptyCaption() {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze("");
        softly.then(stats.totalEmojiCount()).isZero();
        softly.then(stats.textLength()).isZero();
    }

    // ── brak emotek ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnZeroEmojisForPlainText() {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze("Piękne zdjęcie z sesji");
        softly.then(stats.totalEmojiCount()).isZero();
        softly.then(stats.textLength()).isEqualTo(22);
        softly.then(stats.emojiDensity()).isZero();
    }

    // ── proste emotki ─────────────────────────────────────────────────────────

    @Test
    void shouldCountSimpleEmojis() {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze("Piękna sesja 😀❤️🔥");
        softly.then(stats.simpleEmojiCount()).isGreaterThanOrEqualTo(2);
        softly.then(stats.totalEmojiCount()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldCalculateEmojiDensity() {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze("😀😀");
        softly.then(stats.totalEmojiCount()).isEqualTo(2);
        softly.then(stats.emojiDensity()).isGreaterThan(0.0);
    }

    // ── ZWJ sequences ─────────────────────────────────────────────────────────

    @Test
    void shouldCountZwjSequenceAsOne() {
        // 👨‍👩‍👧 — rodzina, trzy emotki + dwa ZWJ
        String family = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67";
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(family);
        softly.then(stats.zwjSequenceCount()).isEqualTo(1);
    }

    @Test
    void shouldCountMultipleZwjSequences() {
        // dwie sekwencje ZWJ
        String family = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67";
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(family + " " + family);
        softly.then(stats.zwjSequenceCount()).isEqualTo(2);
    }

    // ── keycap sequences ──────────────────────────────────────────────────────

    @Test
    void shouldCountKeycapSequences() {
        // 1️⃣ 2️⃣ 3️⃣
        String keycaps = "1\uFE0F\u20E3 2\uFE0F\u20E3 3\uFE0F\u20E3";
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(keycaps);
        softly.then(stats.keycapCount()).isEqualTo(3);
        softly.then(stats.totalEmojiCount()).isEqualTo(3);
    }

    @Test
    void shouldCountHashKeycap() {
        // *️⃣
        String keycap = "*\uFE0F\u20E3";
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(keycap);
        softly.then(stats.keycapCount()).isEqualTo(1);
    }

    // ── mixed content ─────────────────────────────────────────────────────────

    @Test
    void shouldHandleMixedEmojiTypes() {
        // prosty + keycap + tekst
        String mixed = "Krok 1\uFE0F\u20E3 zrób sesję 📸 i ciesz się 😊";
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(mixed);
        softly.then(stats.keycapCount()).isEqualTo(1);
        softly.then(stats.totalEmojiCount()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldNotCountHashtagSymbolAsEmoji() {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze("#fotografia #boudoir");
        softly.then(stats.totalEmojiCount()).isZero();
    }

    // ── density ───────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "'😀😀😀', 3, 3, 1.0",
            "'abc', 3, 0, 0.0",
    })
    void shouldCalculateDensityCorrectly(String text, int expectedLength,
                                          int expectedEmojis, double expectedDensity) {
        EmojiAnalyzer.EmojiStats stats = analyzer.analyze(text);
        softly.then(stats.textLength()).isEqualTo(expectedLength);
        softly.then(stats.totalEmojiCount()).isEqualTo(expectedEmojis);
        softly.then(stats.emojiDensity()).isEqualTo(expectedDensity);
    }
}