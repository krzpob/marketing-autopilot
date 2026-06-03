package pl.autopilot.datacollector.infrastructure.facebook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookInsightsResponse {
    // TODO: uzupełnić pola po analizie odpowiedzi Facebook Insights API
    private List<Object> data;
}