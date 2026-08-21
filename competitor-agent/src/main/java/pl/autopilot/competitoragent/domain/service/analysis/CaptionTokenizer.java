package pl.autopilot.competitoragent.domain.service.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CaptionTokenizer {

    private static final Pattern WHITESPACE     = Pattern.compile("\\s+");
    private static final Pattern HASHTAG        = Pattern.compile("#\\w+");
    private static final Pattern MENTION        = Pattern.compile("@\\w+");
    private static final Pattern LINE_BREAK     = Pattern.compile("\\n");
    private static final Pattern PUNCTUATION    = Pattern.compile("[.!?,;:]");
    private static final Pattern URL            = Pattern.compile(
            "https?://\\S+|www\\.\\S+");

    private final EmojiAnalyzer emojiAnalyzer;
    private final CtaAnalyzer   ctaAnalyzer;

    public CaptionTokens tokenize(String caption) {
        if (caption == null || caption.isBlank()) {
            return CaptionTokens.empty();
        }

        // ── czysty tekst — bez hashtagów, mencji, URL, emotek ────────────────
        String clean = URL.matcher(caption).replaceAll(" ");
        clean = HASHTAG.matcher(clean).replaceAll(" ");
        clean = MENTION.matcher(clean).replaceAll(" ");

        // ── metryki tekstowe ──────────────────────────────────────────────────
        int charCount          = caption.codePointCount(0, caption.length());
        int wordCount          = countWords(clean);
        int lineBreakCount     = count(LINE_BREAK, caption);
        int hashtagCount       = count(HASHTAG, caption);
        int mentionCount       = count(MENTION, caption);
        int urlCount           = count(URL, caption);
        int punctuationCount   = count(PUNCTUATION, caption);
        boolean hasLineBreaks  = lineBreakCount > 0;

        // ── analizy ───────────────────────────────────────────────────────────
        EmojiAnalyzer.EmojiStats emojiStats = emojiAnalyzer.analyze(caption);
        CtaAnalyzer.CtaStats     ctaStats   = ctaAnalyzer.analyze(caption);

        return CaptionTokens.builder()
                .charCount(charCount)
                .wordCount(wordCount)
                .lineBreakCount(lineBreakCount)
                .hasLineBreaks(hasLineBreaks)
                .hashtagCount(hashtagCount)
                .mentionCount(mentionCount)
                .urlCount(urlCount)
                .punctuationCount(punctuationCount)
                .emojiStats(emojiStats)
                .ctaStats(ctaStats)
                .build();
    }

    private int countWords(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        return WHITESPACE.split(trimmed).length;
    }

    private int count(Pattern pattern, String text) {
        java.util.regex.Matcher m = pattern.matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── model ─────────────────────────────────────────────────────────────────

    public record CaptionTokens(
            int charCount,
            int wordCount,
            int lineBreakCount,
            boolean hasLineBreaks,
            int hashtagCount,
            int mentionCount,
            int urlCount,
            int punctuationCount,
            EmojiAnalyzer.EmojiStats emojiStats,
            CtaAnalyzer.CtaStats     ctaStats
    ) {
        static CaptionTokens empty() {
            return new CaptionTokens(
                    0, 0, 0, false, 0, 0, 0, 0,
                    EmojiAnalyzer.EmojiStats.empty(),
                    CtaAnalyzer.CtaStats.empty()
            );
        }

        static Builder builder() { return new Builder(); }

        static class Builder {
            private int charCount;
            private int wordCount;
            private int lineBreakCount;
            private boolean hasLineBreaks;
            private int hashtagCount;
            private int mentionCount;
            private int urlCount;
            private int punctuationCount;
            private EmojiAnalyzer.EmojiStats emojiStats;
            private CtaAnalyzer.CtaStats     ctaStats;

            Builder charCount(int v)        { charCount = v;        return this; }
            Builder wordCount(int v)        { wordCount = v;        return this; }
            Builder lineBreakCount(int v)   { lineBreakCount = v;   return this; }
            Builder hasLineBreaks(boolean v){ hasLineBreaks = v;    return this; }
            Builder hashtagCount(int v)     { hashtagCount = v;     return this; }
            Builder mentionCount(int v)     { mentionCount = v;     return this; }
            Builder urlCount(int v)         { urlCount = v;         return this; }
            Builder punctuationCount(int v) { punctuationCount = v; return this; }
            Builder emojiStats(EmojiAnalyzer.EmojiStats v) { emojiStats = v; return this; }
            Builder ctaStats(CtaAnalyzer.CtaStats v)       { ctaStats = v;   return this; }

            CaptionTokens build() {
                return new CaptionTokens(charCount, wordCount, lineBreakCount,
                        hasLineBreaks, hashtagCount, mentionCount, urlCount,
                        punctuationCount, emojiStats, ctaStats);
            }
        }
    }
}