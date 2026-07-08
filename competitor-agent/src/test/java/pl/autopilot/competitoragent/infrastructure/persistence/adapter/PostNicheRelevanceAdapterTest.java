package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.domain.model.PostNicheRelevance;
import pl.autopilot.competitoragent.domain.model.PostSourceType;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.PostNicheRelevanceEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.PostNicheRelevanceJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostNicheRelevanceAdapterTest {

    @Mock
    private PostNicheRelevanceJpaRepository repository;

    @InjectMocks
    private PostNicheRelevanceAdapter adapter;

    // ── nowy rekord ───────────────────────────────────────────────────────────

    @Test
    void shouldCreateNewEntityWhenNotExists() {
        // given
        given(repository.findByIgMediaIdAndSourceTypeAndOwnerIgId(
                "media123", "COMPETITOR_POST", "owner1"))
                .willReturn(Optional.empty());
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        PostNicheRelevance relevance = aRelevance();

        // when
        adapter.save(relevance);

        // then
        ArgumentCaptor<PostNicheRelevanceEntity> captor =
                ArgumentCaptor.forClass(PostNicheRelevanceEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());

        PostNicheRelevanceEntity saved = captor.getValue();
        then(saved.getId()).isNotNull();
        then(saved.getIgMediaId()).isEqualTo("media123");
        then(saved.getSourceType()).isEqualTo("COMPETITOR_POST");
        then(saved.getOwnerIgId()).isEqualTo("owner1");
        then(saved.getMatchedHashtags()).containsExactly("fotografia", "boudoir");
        then(saved.getWeight()).isEqualTo(0.5);
        then(saved.getComputedAt()).isNotNull();
    }

    // ── upsert istniejącego ──────────────────────────────────────────────────

    @Test
    void shouldUpdateExistingEntityPreservingId() {
        // given
        UUID existingId = UUID.randomUUID();
        PostNicheRelevanceEntity existing = new PostNicheRelevanceEntity();
        existing.setId(existingId);
        existing.setIgMediaId("media123");
        existing.setSourceType("COMPETITOR_POST");
        existing.setOwnerIgId("owner1");
        existing.setWeight(0.25);

        given(repository.findByIgMediaIdAndSourceTypeAndOwnerIgId(
                "media123", "COMPETITOR_POST", "owner1"))
                .willReturn(Optional.of(existing));
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        PostNicheRelevance relevance = aRelevance();

        // when
        adapter.save(relevance);

        // then
        ArgumentCaptor<PostNicheRelevanceEntity> captor =
                ArgumentCaptor.forClass(PostNicheRelevanceEntity.class);
        BDDMockito.then(repository).should().save(captor.capture());

        PostNicheRelevanceEntity saved = captor.getValue();
        then(saved.getId()).isEqualTo(existingId); // ten sam rekord
        then(saved.getWeight()).isEqualTo(0.5);     // zaktualizowana wartość
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PostNicheRelevance aRelevance() {
        return PostNicheRelevance.builder()
                .igMediaId("media123")
                .sourceType(PostSourceType.COMPETITOR_POST)
                .ownerIgId("owner1")
                .matchedHashtags(List.of("fotografia", "boudoir"))
                .weight(0.5)
                .build();
    }
}