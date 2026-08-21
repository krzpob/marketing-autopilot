package pl.autopilot.competitoragent.domain.service.analysis;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class CaptionTokenizerTest {

    // używamy prawdziwych analizatorów — CaptionTokenizer to agregat
    private final EmojiAnalyzer    emojiAnalyzer = new EmojiAnalyzer();
    private final CtaAnalyzer      ctaAnalyzer   = new CtaAnalyzer();
    private final CaptionTokenizer tokenizer     =
            new CaptionTokenizer(emojiAnalyzer, ctaAnalyzer);

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    // ── null / empty ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyTokensForNullCaption() {
        CaptionTokenizer.CaptionTokens tokens = tokenizer.tokenize(null);
        softly.then(tokens.charCount()).isZero();
        softly.then(tokens.wordCount()).isZero();
        softly.then(tokens.hashtagCount()).isZero();
        softly.then(tokens.emojiStats().totalEmojiCount()).isZero();
        softly.then(tokens.ctaStats().hasCta()).isFalse();
    }

    @Test
    void shouldReturnEmptyTokensForBlankCaption() {
        CaptionTokenizer.CaptionTokens tokens = tokenizer.tokenize("   ");
        softly.then(tokens.charCount()).isZero();
        softly.then(tokens.wordCount()).isZero();
    }

    // ── metryki tekstowe ──────────────────────────────────────────────────────

    @Test
    void shouldCountCharsAndWords() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Piękna sesja zdjęciowa");

        softly.then(tokens.charCount()).isEqualTo(22);
        softly.then(tokens.wordCount()).isEqualTo(3);
        softly.then(tokens.hashtagCount()).isZero();
        softly.then(tokens.mentionCount()).isZero();
    }

    @Test
    void shouldCountHashtags() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Sesja #fotografia #boudoir #portret");

        softly.then(tokens.hashtagCount()).isEqualTo(3);
        softly.then(tokens.wordCount()).isEqualTo(1); // tylko "Sesja" po usunięciu hashtagów
    }

    @Test
    void shouldCountMentions() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Dziękuję @modelka @makijaz za współpracę");

        softly.then(tokens.mentionCount()).isEqualTo(2);
        softly.then(tokens.wordCount()).isEqualTo(3); // "Dziękuję", "za", "współpracę"
    }

    @Test
    void shouldCountLineBreaks() {
        CaptionTokenizer.CaptionTokens tokens = tokenizer.tokenize("""
                Piękna sesja.
                Zapraszam do kontaktu.
                Link w bio.
                """);

        softly.then(tokens.lineBreakCount()).isGreaterThanOrEqualTo(2);
        softly.then(tokens.hasLineBreaks()).isTrue();
    }

    @Test
    void shouldDetectNoLineBreaksInSingleLine() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Piękna sesja zdjęciowa");

        softly.then(tokens.hasLineBreaks()).isFalse();
        softly.then(tokens.lineBreakCount()).isZero();
    }

    @Test
    void shouldCountUrls() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Sprawdź https://example.com i www.foto.pl");

        softly.then(tokens.urlCount()).isEqualTo(2);
    }

    @Test
    void shouldCountPunctuation() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Piękna sesja! Zapraszam. Pytania?");

        softly.then(tokens.punctuationCount()).isEqualTo(3);
    }

    // ── integracja z EmojiAnalyzer ────────────────────────────────────────────

    @Test
    void shouldDelegateEmojiAnalysisToEmojiAnalyzer() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Piękna sesja 😍🔥");

        softly.then(tokens.emojiStats().totalEmojiCount()).isGreaterThanOrEqualTo(2);
        softly.then(tokens.emojiStats().simpleEmojiCount()).isGreaterThanOrEqualTo(2);
    }

    // ── integracja z CtaAnalyzer ──────────────────────────────────────────────

    @Test
    void shouldDelegateCtaAnalysisToCtaAnalyzer() {
        CaptionTokenizer.CaptionTokens tokens =
                tokenizer.tokenize("Zapisz ten post! Napisz SESJA w komentarzu.");

        softly.then(tokens.ctaStats().hasCta()).isTrue();
        softly.then(tokens.ctaStats().detectedTypes()).contains(
                CtaAnalyzer.CtaType.CTA_SAVE,
                CtaAnalyzer.CtaType.CTA_COMMENT
        );
    }

    // ── realistyczny caption ──────────────────────────────────────────────────

    @Test
    void shouldTokenizeRealisticInstagramCaption() {
        String caption = """
                Sesja boudoir to nie tylko zdjęcia — to doświadczenie! 😍✨
                
                Napisz SESJA w komentarzu a odezwę się z detalami.
                Więcej realizacji znajdziesz klikając link w bio.
                
                @modelka dziękuję za zaufanie 🙏
                
                #fotografia #boudoir #sesja #portret
                """;

        CaptionTokenizer.CaptionTokens tokens = tokenizer.tokenize(caption);

        softly.then(tokens.hashtagCount()).isEqualTo(4);
        softly.then(tokens.mentionCount()).isEqualTo(1);
        softly.then(tokens.hasLineBreaks()).isTrue();
        softly.then(tokens.emojiStats().totalEmojiCount()).isGreaterThanOrEqualTo(3);
        softly.then(tokens.ctaStats().hasCta()).isTrue();
        softly.then(tokens.ctaStats().detectedTypes()).contains(
                CtaAnalyzer.CtaType.CTA_COMMENT,
                CtaAnalyzer.CtaType.CTA_LINK
        );
    }
}