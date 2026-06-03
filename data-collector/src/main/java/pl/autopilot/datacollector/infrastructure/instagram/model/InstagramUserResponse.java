package pl.autopilot.datacollector.infrastructure.instagram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramUserResponse {
    private String id;
    private String username;
}