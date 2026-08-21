package pl.autopilot.competitoragent.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.autopilot.competitoragent.domain.model.PostNicheRelevance;
import pl.autopilot.competitoragent.domain.port.out.PostNicheRelevancePort;
import pl.autopilot.competitoragent.infrastructure.persistence.entity.PostNicheRelevanceEntity;
import pl.autopilot.competitoragent.infrastructure.persistence.repository.PostNicheRelevanceJpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostNicheRelevanceAdapter implements PostNicheRelevancePort {

    private final PostNicheRelevanceJpaRepository repository;

    @Override
    public void save(PostNicheRelevance relevance) {
        Optional<PostNicheRelevanceEntity> existing =
                repository.findByIgMediaIdAndSourceTypeAndOwnerIgId(
                        relevance.getIgMediaId(),
                        relevance.getSourceType().name(),
                        relevance.getOwnerIgId());

        PostNicheRelevanceEntity entity = existing.orElseGet(() -> {
            PostNicheRelevanceEntity e = new PostNicheRelevanceEntity();
            e.setId(UUID.randomUUID());
            e.setIgMediaId(relevance.getIgMediaId());
            e.setSourceType(relevance.getSourceType().name());
            e.setOwnerIgId(relevance.getOwnerIgId());
            return e;
        });

        entity.setMatchedHashtags(relevance.getMatchedHashtags());
        entity.setWeight(relevance.getWeight());
        entity.setComputedAt(Instant.now());

        repository.save(entity);
    }
}