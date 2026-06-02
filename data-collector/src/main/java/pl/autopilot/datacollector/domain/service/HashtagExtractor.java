package pl.autopilot.datacollector.domain.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class HashtagExtractor {

    private static final Pattern HASHTAG_PATTERN  = Pattern.compile("#(\\w+)");
    private static final Pattern MENTION_PATTERN  = Pattern.compile("@(\\w+)");

    public List<String> extractHashtags(String caption) {
        if (caption == null || caption.isBlank()) return List.of();
        return HASHTAG_PATTERN.matcher(caption)
                .results()
                .map(r -> r.group(1).toLowerCase())
                .distinct()
                .toList();
    }

    public List<String> extractMentions(String caption) {
        if (caption == null || caption.isBlank()) return List.of();
        return MENTION_PATTERN.matcher(caption)
                .results()
                .map(r -> r.group(1).toLowerCase())
                .distinct()
                .toList();
    }
}