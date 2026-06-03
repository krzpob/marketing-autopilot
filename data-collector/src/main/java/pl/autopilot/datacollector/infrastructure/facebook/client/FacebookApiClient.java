package pl.autopilot.datacollector.infrastructure.facebook.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacebookApiClient {

    private final WebClient webClient;
    private final FacebookApiProperties properties;

    // TODO: B2-x — implementacja Facebook Pages API
}