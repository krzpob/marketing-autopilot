package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.MonitoredProfileProjectionEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.MonitoredProfileProjectionJpaRepository;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MonitoredProfileLookupAdapterTest {

    @Mock
    private MonitoredProfileProjectionJpaRepository repository;

    @InjectMocks
    private MonitoredProfileLookupAdapter adapter;

    @Test
    void shouldReturnDistinctActiveCompetitorsForOwner() {
        // given
        given(repository.findAllByOwnerIgIdAndActiveTrue("owner1"))
                .willReturn(List.of(
                        anEntity("owner1", "competitor1"),
                        anEntity("owner1", "competitor2")));

        // when
        List<String> result = adapter.findActiveCompetitorsForOwner("owner1");

        // then
        then(result).containsExactlyInAnyOrder("competitor1", "competitor2");
    }

    @Test
    void shouldReturnEmptyListWhenOwnerObservesNoCompetitors() {
        // given
        given(repository.findAllByOwnerIgIdAndActiveTrue("owner_unknown"))
                .willReturn(List.of());

        // when / then
        then(adapter.findActiveCompetitorsForOwner("owner_unknown")).isEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MonitoredProfileProjectionEntity anEntity(String ownerIgId, String handle) {
        MonitoredProfileProjectionEntity entity = new MonitoredProfileProjectionEntity();
        entity.setOwnerIgId(ownerIgId);
        entity.setCompetitorIgHandle(handle);
        entity.setActive(true);
        return entity;
    }
}