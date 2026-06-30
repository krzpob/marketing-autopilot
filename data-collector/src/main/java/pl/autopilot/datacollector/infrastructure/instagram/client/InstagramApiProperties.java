package pl.autopilot.datacollector.infrastructure.instagram.client;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Configuration
@ConfigurationProperties(prefix = "instagram.api")
public class InstagramApiProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String graphBaseUrl  = "https://graph.facebook.com/v19.0";
    private String authBaseUrl   = "https://www.facebook.com/dialog/oauth";
    private String tokenBaseUrl  = "https://graph.facebook.com/v19.0/oauth/access_token";
    private String scopes        = "instagram_basic,instagram_manage_insights," +
                                   "pages_show_list,pages_read_engagement";
    private String tokenRefreshCron = "0 0 3 * * *";

}