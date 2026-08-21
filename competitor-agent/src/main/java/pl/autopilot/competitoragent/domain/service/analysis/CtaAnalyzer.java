package pl.autopilot.competitoragent.domain.service.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CtaAnalyzer {

    // ── wzorce per kategoria ──────────────────────────────────────────────────

    private static final Pattern CTA_LINK = Pattern.compile(
        "link\\s+w\\s+bio|sprawdź\\s+link|kliknij|swipe\\s+up|\\w+\\s+w\\s+bio",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern CTA_SAVE = Pattern.compile(
            "zapisz|zachowaj|wróć\\s+do\\s+tego|save\\s+this",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern CTA_COMMENT = Pattern.compile(
            "napisz\\s+w\\s+komentarzu|skomentuj|zostaw\\s+komentarz|" +
            "powiedz\\s+mi\\s+w\\s+komentarzu|tag\\w*\\s+kogoś|" +
            "oznacz\\s+kogoś|napisz\\s+\\w+\\s+w\\s+komentarzu",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern CTA_FOLLOW = Pattern.compile(
            "obserwuj|follow|śledź\\s+nas|zaobserwuj",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern CTA_SHARE = Pattern.compile(
            "udostępnij|podziel\\s+się|share\\s+this|wyślij\\s+dalej",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern CTA_QUESTION = Pattern.compile(
            "\\?\\s*$|\\?\\s*[😀-🙏]+\\s*$",
            Pattern.MULTILINE | Pattern.UNICODE_CHARACTER_CLASS);

    public CtaStats analyze(String caption) {
        if (caption == null || caption.isBlank()) {
            return CtaStats.empty();
        }

        List<CtaType> detected = new ArrayList<>();

        if (matches(CTA_LINK,     caption)) detected.add(CtaType.CTA_LINK);
        if (matches(CTA_SAVE,     caption)) detected.add(CtaType.CTA_SAVE);
        if (matches(CTA_COMMENT,  caption)) detected.add(CtaType.CTA_COMMENT);
        if (matches(CTA_FOLLOW,   caption)) detected.add(CtaType.CTA_FOLLOW);
        if (matches(CTA_SHARE,    caption)) detected.add(CtaType.CTA_SHARE);
        if (matches(CTA_QUESTION, caption)) detected.add(CtaType.CTA_QUESTION);

        return new CtaStats(detected, !detected.isEmpty());
    }

    private boolean matches(Pattern pattern, String text) {
        return pattern.matcher(text).find();
    }

    // ── model ─────────────────────────────────────────────────────────────────

    public enum CtaType {
        CTA_LINK,
        CTA_SAVE,
        CTA_COMMENT,
        CTA_FOLLOW,
        CTA_SHARE,
        CTA_QUESTION
    }

    public record CtaStats(
            List<CtaType> detectedTypes,
            boolean hasCta
    ) {
        static CtaStats empty() {
            return new CtaStats(List.of(), false);
        }
    }
}