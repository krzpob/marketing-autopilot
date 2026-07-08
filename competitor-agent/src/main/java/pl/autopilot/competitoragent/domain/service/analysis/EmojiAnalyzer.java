package pl.autopilot.competitoragent.domain.service.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class EmojiAnalyzer {

    // ZWJ sequences — np. 👨‍👩‍👧 (połączone Zero Width Joiner U+200D)
    private static final Pattern ZWJ_SEQUENCE = Pattern.compile(
            "(?:[\\x{1F000}-\\x{1FFFF}\\x{2600}-\\x{27BF}\\x{FE00}-\\x{FEFF}]" +
            "(?:\\x{200D}[\\x{1F000}-\\x{1FFFF}\\x{2600}-\\x{27BF}])*" +
            "(?:[\\x{1F3FB}-\\x{1F3FF}])?)",
            Pattern.UNICODE_CHARACTER_CLASS);

    // Proste emotki — Basic Multilingual Plane + Supplementary
    private static final Pattern SIMPLE_EMOJI = Pattern.compile(
            "[\\x{1F600}-\\x{1F64F}" +  // Emoticons
            "\\x{1F300}-\\x{1F5FF}" +   // Misc Symbols and Pictographs
            "\\x{1F680}-\\x{1F6FF}" +   // Transport and Map
            "\\x{1F700}-\\x{1F77F}" +   // Alchemical Symbols
            "\\x{1F780}-\\x{1F7FF}" +   // Geometric Shapes Extended
            "\\x{1F800}-\\x{1F8FF}" +   // Supplemental Arrows-C
            "\\x{1F900}-\\x{1F9FF}" +   // Supplemental Symbols and Pictographs
            "\\x{1FA00}-\\x{1FA6F}" +   // Chess Symbols
            "\\x{1FA70}-\\x{1FAFF}" +   // Symbols and Pictographs Extended-A
            "\\x{2600}-\\x{26FF}" +     // Misc symbols
            "\\x{2700}-\\x{27BF}" +     // Dingbats
            "\\x{FE00}-\\x{FEFF}]",     // Variation Selectors
            Pattern.UNICODE_CHARACTER_CLASS);

    // Keycap sequences — np. 1️⃣ 2️⃣ *️⃣
    private static final Pattern KEYCAP = Pattern.compile(
            "[0-9#*]\\x{FE0F}\\x{20E3}",
            Pattern.UNICODE_CHARACTER_CLASS);

    // Skin tone modifiers U+1F3FB–U+1F3FF
    private static final Pattern SKIN_TONE = Pattern.compile(
            "[\\x{1F3FB}-\\x{1F3FF}]",
            Pattern.UNICODE_CHARACTER_CLASS);

    public EmojiStats analyze(String text) {
    if (text == null || text.isEmpty()) {
        return EmojiStats.empty();
    }

    int keycapCount   = count(KEYCAP, text);
    String noKeycaps  = KEYCAP.matcher(text).replaceAll("");

    int skinToneCount = count(SKIN_TONE, noKeycaps);
    int zwjCount      = countZwj(noKeycaps);
    int simpleCount   = count(SIMPLE_EMOJI, noKeycaps)
                        - skinToneCount
                        - zwjCount;

    int totalEmojis   = Math.max(0, simpleCount) + zwjCount + keycapCount;
    int textLength    = text.codePointCount(0, text.length());
    double density    = textLength > 0
                        ? (double) totalEmojis / textLength
                        : 0.0;

    return EmojiStats.builder()
            .simpleEmojiCount(Math.max(0, simpleCount))
            .zwjSequenceCount(zwjCount)
            .keycapCount(keycapCount)
            .totalEmojiCount(totalEmojis)
            .textLength(textLength)
            .emojiDensity(density)
            .build();
}

    private int count(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    private int countZwj(String text) {
        Matcher m = ZWJ_SEQUENCE.matcher(text);
        int count = 0;
        while (m.find()) {
            if (m.group().contains("\u200D")) count++;
        }
        return count;
    }

    public record EmojiStats(
            int simpleEmojiCount,
            int zwjSequenceCount,
            int keycapCount,
            int totalEmojiCount,
            int textLength,
            double emojiDensity
    ) {
        static EmojiStats empty() {
            return new EmojiStats(0, 0, 0, 0, 0, 0.0);
        }

        static Builder builder() { return new Builder(); }

        static class Builder {
            private int simpleEmojiCount;
            private int zwjSequenceCount;
            private int keycapCount;
            private int totalEmojiCount;
            private int textLength;
            private double emojiDensity;

            Builder simpleEmojiCount(int v) { simpleEmojiCount = v; return this; }
            Builder zwjSequenceCount(int v)  { zwjSequenceCount = v;  return this; }
            Builder keycapCount(int v)       { keycapCount = v;       return this; }
            Builder totalEmojiCount(int v)   { totalEmojiCount = v;   return this; }
            Builder textLength(int v)        { textLength = v;        return this; }
            Builder emojiDensity(double v)   { emojiDensity = v;      return this; }
            EmojiStats build() {
                return new EmojiStats(simpleEmojiCount, zwjSequenceCount,
                        keycapCount, totalEmojiCount, textLength, emojiDensity);
            }
        }
    }
}