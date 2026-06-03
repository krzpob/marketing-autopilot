package pl.autopilot.datacollector.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;



@Builder(toBuilder = true)
@Getter
public class AccessToken {
    
    @Builder.Default
    private final UUID id=UUID.randomUUID();
    private final String ownerIgId;
    private final String ownerUsername;
    private final String token;
    private final TokenType tokenType;
    
    @Builder.Default
    private final Instant createdAt = Instant.now();
    private final Instant expiresAt;
    private final Instant refreshedAt;
    
    public enum TokenType {
        SHORT_LIVED,  // ~1 godzina, zwracany po OAuth callback
        LONG_LIVED    // ~60 dni, po wymianie short-lived
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * True jeśli token wygasa za mniej niż 7 dni — sygnał do odświeżenia
     * przez TokenRefreshService zanim wygaśnie.
     */
    public boolean isExpiringSoon() {
        if (expiresAt == null) return false;
        Instant threshold = expiresAt.minusSeconds(7L * 24 * 3600);
        return Instant.now().isAfter(threshold);
    }

    /**
     * Zwraca nową instancję z zaktualizowanym tokenem i datą wygaśnięcia.
     * Używane przez TokenRefreshService po udanym odświeżeniu.
     */
    public AccessToken withRefreshed(String newToken, Instant newExpiresAt) {
        return this.toBuilder()
                .token(newToken)
                .tokenType(TokenType.LONG_LIVED)
                .expiresAt(newExpiresAt)
                .refreshedAt(Instant.now())
                .build();
    }


}