package pl.autopilot.datacollector.infrastructure.facebook.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class FacebookApiClient {

    private final RestClient restClient;
    private final FacebookApiProperties properties;

   public FacebookApiClient(RestClient.Builder builder, FacebookApiProperties properties) {
        this.restClient = builder.build();
        this.properties  = properties;
   }
    // TODO: B2-x — implementacja Facebook Pages API
}
