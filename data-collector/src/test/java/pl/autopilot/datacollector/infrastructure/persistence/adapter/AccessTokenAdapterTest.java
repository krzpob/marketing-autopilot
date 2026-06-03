package pl.autopilot.datacollector.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.AccessToken;
import pl.autopilot.datacollector.infrastructure.persistence.entity.AccessTokenEntity;
import pl.autopilot.datacollector.infrastructure.persistence.repository.AccessTokenJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessTokenAdapterTest {

    @Mock
    private AccessTokenJpaRepository repository;

    @InjectMocks
    private AccessTokenAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        // given
        AccessToken token = aLongLivedToken();
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        adapter.save(token);

        // then
        BDDMockito.then(repository).should().save(any(AccessTokenEntity.class));
    }

    // ── findByOwnerIgId ──────────────────────────────────────────────────────

    @Test
    void shouldReturnMappedDomainTokenWhenFound() {
        // given
        given(repository.findByOwnerIgId("12345678")).willReturn(Optional.of(anEntity()));

        // when
        Optional<AccessToken> result = adapter.findByOwnerIgId("12345678");

        // then
        then(result).isPresent();
        then(result.get().getOwnerIgId()).isEqualTo("12345678");
        then(result.get().getOwnerUsername()).isEqualTo("testuser");
        then(result.get().getToken()).isEqualTo("long-lived-token");
        then(result.get().getTokenType()).isEqualTo(AccessToken.TokenType.LONG_LIVED);
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {
        // given
        given(repository.findByOwnerIgId("unknown")).willReturn(Optional.empty());

        // when
        Optional<AccessToken> result = adapter.findByOwnerIgId("unknown");

        // then
        then(result).isEmpty();
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnAllMappedTokens() {
        // given
        given(repository.findAll()).willReturn(List.of(anEntity(), anEntity()));

        // when
        List<AccessToken> result = adapter.findAll();

        // then
        then(result).hasSize(2);
        then(result).allMatch(t -> t.getOwnerIgId().equals("12345678"));
    }

    // ── findAllExpiringSoon ──────────────────────────────────────────────────

    @Test
void shouldQueryWithSevenDayThresholdWhenFindingExpiringSoon() {
    // given
    given(repository.findAllByExpiresAtBefore(any(Instant.class)))
            .willReturn(List.of(anEntity()));

    Instant beforeCall = Instant.now().plusSeconds(7L * 24 * 3600);

    // when
    List<AccessToken> result = adapter.findAllExpiringSoon();

    // then
    then(result).hasSize(1);

    ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
    BDDMockito.then(repository).should().findAllByExpiresAtBefore(captor.capture());

    Instant threshold = captor.getValue();
    then(threshold).isBetween(
            beforeCall.minusSeconds(5),
            beforeCall.plusSeconds(5)
    );
}

    @Test
    void shouldReturnEmptyListWhenNoTokensExpiringSoon() {
        // given
        given(repository.findAllByExpiresAtBefore(any(Instant.class)))
                .willReturn(List.of());

        // when
        List<AccessToken> result = adapter.findAllExpiringSoon();

        // then
        then(result).isEmpty();
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void shouldDelegateDeleteToRepository() {
        // when
        adapter.delete("12345678");

        // then
        BDDMockito.then(repository).should().deleteByOwnerIgId("12345678");
    }

    // ── mapowanie round-trip ─────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        // given
        UUID id         = UUID.randomUUID();
        Instant created = Instant.now().minusSeconds(3600);
        Instant expires = Instant.now().plusSeconds(60L * 24 * 3600);

        AccessTokenEntity entity = new AccessTokenEntity();
        entity.setId(id);
        entity.setOwnerIgId("12345678");
        entity.setOwnerUsername("testuser");
        entity.setToken("long-lived-token");
        entity.setTokenType("LONG_LIVED");
        entity.setExpiresAt(expires);
        entity.setCreatedAt(created);
        entity.setRefreshedAt(null);

        given(repository.findByOwnerIgId("12345678")).willReturn(Optional.of(entity));

        // when
        AccessToken result = adapter.findByOwnerIgId("12345678").orElseThrow();

        // then
        then(result.getId()).isEqualTo(id);
        then(result.getOwnerIgId()).isEqualTo("12345678");
        then(result.getOwnerUsername()).isEqualTo("testuser");
        then(result.getToken()).isEqualTo("long-lived-token");
        then(result.getTokenType()).isEqualTo(AccessToken.TokenType.LONG_LIVED);
        then(result.getExpiresAt()).isEqualTo(expires);
        then(result.getCreatedAt()).isEqualTo(created);
        then(result.getRefreshedAt()).isNull();
    }

    // ── factory helpers ──────────────────────────────────────────────────────

    private AccessToken aLongLivedToken() {
        return AccessToken.builder()
                .ownerIgId("12345678")
                .ownerUsername("testuser")
                .token("long-lived-token")
                .tokenType(AccessToken.TokenType.LONG_LIVED)
                .expiresAt(Instant.now().plusSeconds(60L * 24 * 3600))
                .build();
    }

    private AccessTokenEntity anEntity() {
        AccessTokenEntity entity = new AccessTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setOwnerIgId("12345678");
        entity.setOwnerUsername("testuser");
        entity.setToken("long-lived-token");
        entity.setTokenType("LONG_LIVED");
        entity.setExpiresAt(Instant.now().plusSeconds(60L * 24 * 3600));
        entity.setCreatedAt(Instant.now().minusSeconds(3600));
        return entity;
    }
}