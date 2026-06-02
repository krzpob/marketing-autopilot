package pl.autopilot.datacollector.infrastructure.google.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleMyBusinessResponse {
    // TODO: uzupełnić pola po analizie odpowiedzi Google My Business API
    private List<Object> locations;
}