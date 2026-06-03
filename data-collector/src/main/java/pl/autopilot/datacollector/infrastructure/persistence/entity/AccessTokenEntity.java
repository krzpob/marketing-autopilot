package pl.autopilot.datacollector.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_tokens")
@Getter
@Setter
@NoArgsConstructor
public class AccessTokenEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "owner_ig_id", nullable = false, unique = true)
    private String ownerIgId;

    @Column(name = "owner_username")
    private String ownerUsername;

    @Column(name = "token", nullable = false, length = 2048)
    private String token;

    @Column(name = "token_type", nullable = false)
    private String tokenType;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "refreshed_at")
    private Instant refreshedAt;
}