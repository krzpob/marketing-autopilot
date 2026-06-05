package pl.autopilot.datacollector.infrastructure.facebook.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "facebook.api")
public class FacebookApiProperties {
    private String baseUrl = "https://graph.facebook.com/v19.0";
    private String version  = "v19.0";
}