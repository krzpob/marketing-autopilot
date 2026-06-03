package pl.autopilot.datacollector.infrastructure.google.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchConsoleApiClient {

    private final WebClient webClient;
    private final GoogleApiProperties properties;

    // TODO: B2-x — implementacja Google Search Console API
}