package pl.autopilot.datacollector.infrastructure.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
    String brokerTokenUrl
) {}