package pl.autopilot.datacollector.infrastructure.google.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GoogleMyBusinessApiClient {

    private final RestClient restClient;
    private final GoogleApiProperties properties;

    public GoogleMyBusinessApiClient(RestClient.Builder builder, GoogleApiProperties properties) {
        this.restClient = builder.build();
        this.properties  = properties;
    }

    // TODO: B2-x — implementacja Google My Business API
}