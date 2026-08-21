package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredHashtagProjectionEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredHashtagProjectionJpaRepository;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MonitoredHashtagLookupAdapterTest {

    @Mock
    private MonitoredHashtagProjectionJpaRepository repository;

    @InjectMocks
    private MonitoredHashtagLookupAdapter adapter;

    // ── findOwnersObservingHashtag ────────────────────────────────────────────

    @Test
    void shouldReturnDistinctOwnersObservingHashtag() {
        // given
        given(repository.findAllByHashtagAndActiveTrue("fotografia"))
                .willReturn(List.of(
                        anEntity("owner1", "fotografia"),
                        anEntity("owner2", "fotografia")));

        // when
        List<String> result = adapter.findOwnersObservingHashtag("fotografia");

        // then
        then(result).containsExactlyInAnyOrder("owner1", "owner2");
    }

    @Test
    void shouldReturnEmptyListWhenNoOwnersObserveHashtag() {
        // given
        given(repository.findAllByHashtagAndActiveTrue("unknown"))
                .willReturn(List.of());

        // when / then
        then(adapter.findOwnersObservingHashtag("unknown")).isEmpty();
    }

    // ── countActiveHashtagsForOwner ───────────────────────────────────────────

    @Test
    void shouldCountActiveHashtagsForOwner() {
        // given
        given(repository.findAllByOwnerIgIdAndActiveTrue("owner1"))
                .willReturn(List.of(
                        anEntity("owner1", "fotografia"),
                        anEntity("owner1", "boudoir"),
                        anEntity("owner1", "portret")));

        // when
        int count = adapter.countActiveHashtagsForOwner("owner1");

        // then
        then(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenOwnerObservesNothing() {
        // given
        given(repository.findAllByOwnerIgIdAndActiveTrue("owner_unknown"))
                .willReturn(List.of());

        // when / then
        then(adapter.countActiveHashtagsForOwner("owner_unknown")).isZero();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredHashtagProjectionEntity anEntity(String ownerIgId, String hashtag) {
        MonitoredHashtagProjectionEntity entity = new MonitoredHashtagProjectionEntity();
        entity.setOwnerIgId(ownerIgId);
        entity.setHashtag(hashtag);
        entity.setActive(true);
        return entity;
    }
}