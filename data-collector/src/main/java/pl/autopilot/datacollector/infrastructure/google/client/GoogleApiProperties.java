package pl.autopilot.datacollector.infrastructure.google.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.api")
public class GoogleApiProperties {
    private String myBusinessBaseUrl   = "https://mybusiness.googleapis.com/v4";
    private String searchConsoleBaseUrl = "https://searchconsole.googleapis.com/v1";
}