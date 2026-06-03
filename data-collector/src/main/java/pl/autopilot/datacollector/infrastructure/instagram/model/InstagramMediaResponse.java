package pl.autopilot.datacollector.infrastructure.instagram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramMediaResponse {

    private List<MediaItem> data;
    private Paging paging;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaItem {
        private String id;
        private String shortcode;
        @JsonProperty("media_type")
        private String mediaType;
        private String caption;
        @JsonProperty("media_url")
        private String mediaUrl;
        private String permalink;
        @JsonProperty("like_count")
        private long likeCount;
        @JsonProperty("comments_count")
        private int commentsCount;
        private String timestamp;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paging {
        private Cursors cursors;
        private String next;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Cursors {
            private String before;
            private String after;
        }
    }
}