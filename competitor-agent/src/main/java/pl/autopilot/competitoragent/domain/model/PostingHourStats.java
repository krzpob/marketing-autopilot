package pl.autopilot.competitoragent.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class PostingHourStats {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String competitorUsername;
    private final String mediaType;
    private final short  hourOfDay;

    private final double avgEngagementRate;
    private final int    postCount;
    private final long   totalLikeCount;
    private final long   totalCommentsCount;

    @Builder.Default
    private final Instant lastUpdatedAt = Instant.now();

    /** Przelicza nową średnią po dodaniu nowego posta */
    public PostingHourStats withNewPost(long likeCount, int commentsCount,
                                        long followerCount) {
        long newTotalLikes    = totalLikeCount + likeCount;
        long newTotalComments = totalCommentsCount + commentsCount;
        int  newPostCount     = postCount + 1;

        double postEr = followerCount > 0
                ? (likeCount + commentsCount) * 100.0 / followerCount
                : 0.0;
        double newAvgEr = ((avgEngagementRate * postCount) + postEr) / newPostCount;

        return this.toBuilder()
                .totalLikeCount(newTotalLikes)
                .totalCommentsCount(newTotalComments)
                .postCount(newPostCount)
                .avgEngagementRate(newAvgEr)
                .lastUpdatedAt(Instant.now())
                .build();
    }
}