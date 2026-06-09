package pl.autopilot.datacollector.infrastructure.instagram.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
record AppUsage(
        @JsonProperty("call_count")    int callCount,
        @JsonProperty("total_cputime") int totalCputime,
        @JsonProperty("total_time")    int totalTime
) {
    private static final int WARNING_THRESHOLD = 80;
    private static final int LIMIT_THRESHOLD   = 100;

    boolean isApproachingLimit() {
        return callCount   > WARNING_THRESHOLD
            || totalCputime > WARNING_THRESHOLD
            || totalTime    > WARNING_THRESHOLD;
    }

    boolean isAtLimit() {
        return callCount   >= LIMIT_THRESHOLD
            || totalCputime >= LIMIT_THRESHOLD
            || totalTime    >= LIMIT_THRESHOLD;
    }

    int maxUsage() {
        return Math.max(callCount, Math.max(totalCputime, totalTime));
    }
}