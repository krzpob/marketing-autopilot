package pl.autopilot.datacollector.infrastructure.google.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchConsoleResponse {
    // TODO: uzupełnić pola po analizie odpowiedzi Search Console API
    private List<Object> rows;
}