package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.CompetitorPost;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.CompetitorPostEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.CompetitorPostJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CompetitorPostAdapterTest {

    @Mock
    private CompetitorPostJpaRepository repository;

    @InjectMocks
    private CompetitorPostAdapter adapter;

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void shouldPersistMappedEntityWhenSaving() {
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        adapter.save(aPost());

        BDDMockito.then(repository).should().save(any(CompetitorPostEntity.class));
    }

    // ── findByIgMediaId ───────────────────────────────────────────────────────

    @Test
    void shouldReturnMappedPostWhenFound() {
        given(repository.findByIgMediaId("media123"))
                .willReturn(Optional.of(anEntity()));

        Optional<CompetitorPost> result = adapter.findByIgMediaId("media123");

        then(result).isPresent();
        then(result.get().getIgMediaId()).isEqualTo("media123");
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        given(repository.findByIgMediaId("unknown"))
                .willReturn(Optional.empty());

        then(adapter.findByIgMediaId("unknown")).isEmpty();
    }

    // ── existsByIgMediaId ─────────────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenPostExists() {
        given(repository.existsByIgMediaId("media123")).willReturn(true);

        then(adapter.existsByIgMediaId("media123")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPostNotExists() {
        given(repository.existsByIgMediaId("unknown")).willReturn(false);

        then(adapter.existsByIgMediaId("unknown")).isFalse();
    }

    // ── findLatestByUsername ──────────────────────────────────────────────────

    @Test
    void shouldReturnLatestPostsForUsername() {
        given(repository.findTop30ByCompetitorUsernameOrderByPublishedAtDesc("fotografik_waw"))
                .willReturn(List.of(anEntity(), anEntity()));

        List<CompetitorPost> result = adapter.findLatestByUsername("fotografik_waw", 30);

        then(result).hasSize(2);
    }

    @Test
    void shouldRespectLimitWhenFetchingLatestPosts() {
        given(repository.findTop30ByCompetitorUsernameOrderByPublishedAtDesc("fotografik_waw"))
                .willReturn(List.of(anEntity(), anEntity(), anEntity()));

        List<CompetitorPost> result = adapter.findLatestByUsername("fotografik_waw", 2);

        then(result).hasSize(2);
    }

    // ── round-trip mapping ────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllFieldsInEntityToDomainMapping() {
        UUID    id          = UUID.randomUUID();
        Instant publishedAt = Instant.parse("2024-06-15T10:00:00Z");
        Instant collectedAt = Instant.parse("2024-06-15T11:00:00Z");

        CompetitorPostEntity entity = new CompetitorPostEntity();
        entity.setId(id);
        entity.setIgMediaId("media123");
        entity.setShortcode("ABC123");
        entity.setCompetitorUsername("fotografik_waw");
        entity.setOwnerIgId("owner_ig_123");
        entity.setMediaType("IMAGE");
        entity.setCaption("Piękne zdjęcie #fotografia");
        entity.setHashtags(List.of("fotografia"));
        entity.setMediaUrl("https://media.url");
        entity.setLikeCount(100L);
        entity.setCommentsCount(10);
        entity.setFollowerCountAtCollection(5000L);
        entity.setPublishedAt(publishedAt);
        entity.setCollectedAt(collectedAt);
        entity.setCreatedAt(Instant.now());

        given(repository.findByIgMediaId("media123"))
                .willReturn(Optional.of(entity));

        CompetitorPost result = adapter.findByIgMediaId("media123").orElseThrow();

        then(result.getId()).isEqualTo(id);
        then(result.getIgMediaId()).isEqualTo("media123");
        then(result.getShortcode()).isEqualTo("ABC123");
        then(result.getCompetitorUsername()).isEqualTo("fotografik_waw");
        then(result.getMediaType()).isEqualTo(CompetitorPost.MediaType.IMAGE);
        then(result.getCaption()).isEqualTo("Piękne zdjęcie #fotografia");
        then(result.getLikeCount()).isEqualTo(100L);
        then(result.getCommentsCount()).isEqualTo(10);
        then(result.getFollowerCountAtCollection()).isEqualTo(5000L);
        then(result.getPublishedAt()).isEqualTo(publishedAt);
        then(result.getCollectedAt()).isEqualTo(collectedAt);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CompetitorPost aPost() {
        return CompetitorPost.builder()
                .igMediaId("media123")
                .shortcode("ABC123")
                .competitorUsername("fotografik_waw")
                .ownerIgId("owner_ig_123")
                .mediaType(CompetitorPost.MediaType.IMAGE)
                .likeCount(100L)
                .commentsCount(10)
                .followerCountAtCollection(5000L)
                .publishedAt(Instant.now().minusSeconds(3600))
                .collectedAt(Instant.now())
                .build();
    }

    private CompetitorPostEntity anEntity() {
        CompetitorPostEntity entity = new CompetitorPostEntity();
        entity.setId(UUID.randomUUID());
        entity.setIgMediaId("media123");
        entity.setShortcode("ABC123");
        entity.setCompetitorUsername("fotografik_waw");
        entity.setOwnerIgId("owner_ig_123");
        entity.setMediaType("IMAGE");
        entity.setLikeCount(100L);
        entity.setCommentsCount(10);
        entity.setFollowerCountAtCollection(5000L);
        entity.setPublishedAt(Instant.now().minusSeconds(3600));
        entity.setCollectedAt(Instant.now());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
