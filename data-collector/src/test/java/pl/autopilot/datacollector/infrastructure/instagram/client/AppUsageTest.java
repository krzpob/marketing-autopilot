package pl.autopilot.datacollector.infrastructure.instagram.client;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SoftAssertionsExtension.class)
class AppUsageTest {

    @InjectSoftAssertions
    private BDDSoftAssertions softly;

    @Test
    void shouldNotWarnWhenUsageLow() {
        // given
        AppUsage usage = new AppUsage(10, 5, 15);

        // then
        softly.then(usage.isApproachingLimit()).isFalse();
        softly.then(usage.isAtLimit()).isFalse();
        softly.then(usage.maxUsage()).isEqualTo(15);
    }

    @Test
    void shouldWarnWhenAnyFieldExceeds80Percent() {
        then(new AppUsage(85, 10, 10).isApproachingLimit()).isTrue();
        then(new AppUsage(10, 85, 10).isApproachingLimit()).isTrue();
        then(new AppUsage(10, 10, 85).isApproachingLimit()).isTrue();
    }

    @Test
    void shouldDetectLimitWhenAnyFieldReaches100Percent() {
        then(new AppUsage(100, 0, 0).isAtLimit()).isTrue();
        then(new AppUsage(0, 100, 0).isAtLimit()).isTrue();
        then(new AppUsage(0, 0, 100).isAtLimit()).isTrue();
    }

    @Test
    void shouldNotDetectLimitAt99Percent() {
        then(new AppUsage(99, 99, 99).isAtLimit()).isFalse();
        then(new AppUsage(99, 99, 99).isApproachingLimit()).isTrue();
    }
}