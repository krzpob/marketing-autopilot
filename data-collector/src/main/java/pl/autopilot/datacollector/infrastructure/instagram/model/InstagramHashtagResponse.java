package pl.autopilot.datacollector.infrastructure.instagram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramHashtagResponse {

    // GET /ig_hashtag_search zwraca listę z jednym elementem
    private List<HashtagData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HashtagData {
        private String id;          // Instagram wewnętrzne ID hashtagу
        private String name;        // hashtag bez #
    }
}
