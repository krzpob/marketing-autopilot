package pl.autopilot.datacollector.infrastructure.instagram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramProfileResponse {

    // Business Discovery API — GET /{ig-user-id}?fields=business_discovery.as(...)
    private String id;
    private String username;
    private String name;
    private String biography;
    private String website;

    @JsonProperty("profile_picture_url")
    private String profilePictureUrl;

    @JsonProperty("followers_count")
    private long followersCount;

    @JsonProperty("media_count")
    private int mediaCount;

    @JsonProperty("account_type")
    private String accountType;     // BUSINESS | MEDIA_CREATOR | PERSONAL
}