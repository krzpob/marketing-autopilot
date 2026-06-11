package pl.autopilot.datacollector.infrastructure.instagram.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import pl.autopilot.datacollector.domain.model.CollectedPost;
import pl.autopilot.datacollector.domain.service.HashtagExtractor;
import pl.autopilot.datacollector.infrastructure.instagram.InstagramUtils;
import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramMediaResponse;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstagramMediaMapper {

    private final HashtagExtractor hashtagExtractor;


    public CollectedPost toDomain(InstagramMediaResponse.MediaItem item,
                                  String ownerIgId,
                                  String ownerUsername) {
        return CollectedPost.builder()
                .shortcode(item.getShortcode())
                .ownerIgId(ownerIgId)
                .ownerUsername(ownerUsername)
                .mediaType(mapMediaType(item.getMediaType()))
                .caption(item.getCaption())
                .hashtags(hashtagExtractor.extractHashtags(item.getCaption()))
                .mentions(hashtagExtractor.extractMentions(item.getCaption()))
                .mediaUrl(item.getMediaUrl())
                .permalink(item.getPermalink())
                .likeCount(item.getLikeCount())
                .commentsCount(item.getCommentsCount())
                .publishedAt(InstagramUtils.parseTimestamp(item.getTimestamp()))
                .build();
    }

    private CollectedPost.MediaType mapMediaType(String raw) {
        if (raw == null) return CollectedPost.MediaType.UNKNOWN;
        return switch (raw.toUpperCase()) {
            case "IMAGE"          -> CollectedPost.MediaType.IMAGE;
            case "VIDEO"          -> CollectedPost.MediaType.VIDEO;
            case "CAROUSEL_ALBUM" -> CollectedPost.MediaType.CAROUSEL_ALBUM;
            case "REELS"          -> CollectedPost.MediaType.REEL;
            default               -> CollectedPost.MediaType.UNKNOWN;
        };
    }

}