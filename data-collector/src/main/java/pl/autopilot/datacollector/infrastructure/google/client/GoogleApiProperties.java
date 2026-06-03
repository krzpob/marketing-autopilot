package pl.autopilot.datacollector.infrastructure.google.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "google.api")
public class GoogleApiProperties {
    private String myBusinessBaseUrl   = "https://mybusiness.googleapis.com/v4";
    private String searchConsoleBaseUrl = "https://searchconsole.googleapis.com/v1";
}