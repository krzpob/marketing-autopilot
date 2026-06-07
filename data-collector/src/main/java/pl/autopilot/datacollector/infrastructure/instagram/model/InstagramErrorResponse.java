package pl.autopilot.datacollector.infrastructure.instagram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramErrorResponse {

    private ErrorDetail error;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetail {
        private String message;
        private String type;
        private int code;
        @JsonProperty("error_subcode")
        private int errorSubcode;
        @JsonProperty("fbtrace_id")
        private String fbtraceId;
    }
}