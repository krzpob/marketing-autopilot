package pl.autopilot.datacollector.infrastructure.google.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
@Slf4j
@Component
public class SearchConsoleApiClient {

    private final RestClient restClient;
    private final GoogleApiProperties properties;

    public SearchConsoleApiClient(RestClient.Builder builder, GoogleApiProperties properties) {
        this.restClient = builder.build();
        this.properties  = properties;
    }

    // TODO: B2-x — implementacja Google Search Console API
}