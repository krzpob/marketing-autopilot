package pl.autopilot.datacollector.infrastructure.instagram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramUserResponse {
    private String id;      // Facebook User ID
    private String name;

    private AccountList accounts;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountList {
        private List<PageData> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageData {
        @JsonProperty("instagram_business_account")
        private IgAccount instagramBusinessAccount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IgAccount {
        private String id;
        private String username;
    }

    /** Wyciąga Instagram Business Account ID — null jeśli brak konta */
    public String getInstagramAccountId() {
        if (accounts == null || accounts.getData() == null) return null;
        return accounts.getData().stream()
                .filter(p -> p.getInstagramBusinessAccount() != null)
                .map(p -> p.getInstagramBusinessAccount().getId())
                .findFirst()
                .orElse(null);
    }

    public String getInstagramUsername() {
        if (accounts == null || accounts.getData() == null) return null;
        return accounts.getData().stream()
                .filter(p -> p.getInstagramBusinessAccount() != null)
                .map(p -> p.getInstagramBusinessAccount().getUsername())
                .findFirst()
                .orElse(null);
    }
}