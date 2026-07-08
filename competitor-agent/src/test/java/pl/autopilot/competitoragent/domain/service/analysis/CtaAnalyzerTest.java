package pl.autopilot.competitoragent.domain.service.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.BDDAssertions.then;

class CtaAnalyzerTest {

    private final CtaAnalyzer analyzer = new CtaAnalyzer();

    // ── null / empty ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyStatsForNullCaption() {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(null);
        then(stats.hasCta()).isFalse();
        then(stats.detectedTypes()).isEmpty();
    }

    @Test
    void shouldReturnEmptyStatsForBlankCaption() {
        CtaAnalyzer.CtaStats stats = analyzer.analyze("   ");
        then(stats.hasCta()).isFalse();
        then(stats.detectedTypes()).isEmpty();
    }

    // ── brak CTA ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnNoCataForPlainCaption() {
        CtaAnalyzer.CtaStats stats = analyzer.analyze("Piękna sesja zdjęciowa w Toruniu.");
        then(stats.hasCta()).isFalse();
        then(stats.detectedTypes()).isEmpty();
    }

    // ── CTA_LINK ──────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "Sprawdź link w bio po więcej info",
        "Kliknij aby dowiedzieć się więcej",
        "Więcej info znajdziesz w bio"
})
    void shouldDetectLinkCta(String caption) {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.detectedTypes()).contains(CtaAnalyzer.CtaType.CTA_LINK);
    }

    // ── CTA_SAVE ──────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "Zapisz ten post na później",
            "Zachowaj i wróć do tego kiedy będziesz gotowa",
    })
    void shouldDetectSaveCta(String caption) {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.detectedTypes()).contains(CtaAnalyzer.CtaType.CTA_SAVE);
    }

    // ── CTA_COMMENT ───────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "Napisz w komentarzu SESJA a odezwę się",
            "Skomentuj jeśli chcesz wiedzieć więcej",
            "Zostaw komentarz z pytaniem",
            "Napisz TAK w komentarzu jeśli chcesz",
            "Oznacz kogoś kto potrzebuje sesji"
    })
    void shouldDetectCommentCta(String caption) {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.detectedTypes()).contains(CtaAnalyzer.CtaType.CTA_COMMENT);
    }

    // ── CTA_FOLLOW ────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "Obserwuj aby nie przegapić kolejnych sesji",
            "Zaobserwuj mój profil po więcej",
    })
    void shouldDetectFollowCta(String caption) {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.detectedTypes()).contains(CtaAnalyzer.CtaType.CTA_FOLLOW);
    }

    // ── CTA_SHARE ─────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "Udostępnij jeśli znasz kogoś kto potrzebuje sesji",
            "Podziel się z przyjaciółką",
    })
    void shouldDetectShareCta(String caption) {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.detectedTypes()).contains(CtaAnalyzer.CtaType.CTA_SHARE);
    }

    // ── CTA_QUESTION ──────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "Marzyłaś kiedyś o takiej sesji?",
            "Co myślisz o tym zdjęciu?",
            "Jakie masz pytania?"
    })
    void shouldDetectQuestionCta(String caption) {
        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.detectedTypes()).contains(CtaAnalyzer.CtaType.CTA_QUESTION);
    }

    // ── wiele CTA jednocześnie ────────────────────────────────────────────────

    @Test
    void shouldDetectMultipleCtaTypes() {
        String caption = """
                Marzyłaś o takiej sesji? 😍
                Napisz SESJA w komentarzu a odezwę się!
                Więcej zdjęć znajdziesz klikając link w bio.
                """;

        CtaAnalyzer.CtaStats stats = analyzer.analyze(caption);
        then(stats.hasCta()).isTrue();
        then(stats.detectedTypes()).contains(
                CtaAnalyzer.CtaType.CTA_QUESTION,
                CtaAnalyzer.CtaType.CTA_COMMENT,
                CtaAnalyzer.CtaType.CTA_LINK
        );
    }

    // ── case insensitive ──────────────────────────────────────────────────────

    @Test
    void shouldDetectCtaCaseInsensitive() {
        then(analyzer.analyze("ZAPISZ ten post").detectedTypes())
                .contains(CtaAnalyzer.CtaType.CTA_SAVE);
        then(analyzer.analyze("zapisz ten post").detectedTypes())
                .contains(CtaAnalyzer.CtaType.CTA_SAVE);
        then(analyzer.analyze("Zapisz ten post").detectedTypes())
                .contains(CtaAnalyzer.CtaType.CTA_SAVE);
    }
}