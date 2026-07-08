package pl.autopilot.datacollector.infrastructure.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import pl.autopilot.common.event.MonitoredProfileEvent;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MonitoredProfileEventKafkaAdapterTest {

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private MonitoredProfileEventKafkaAdapter adapter;

    @Test
    void shouldPublishEventWithCorrectFields() {
        // given
        MonitoredProfile profile = MonitoredProfile.builder()
                .ownerIgId("owner_123")
                .competitorIgHandle("rywal_pl")
                .active(true)
                .build();

        given(streamBridge.send(eq("monitored-profile-out-0"), any())).willReturn(true);

        // when
        adapter.publish(profile, ChangeType.ADDED);

        // then
        ArgumentCaptor<MonitoredProfileEvent> captor =
                ArgumentCaptor.forClass(MonitoredProfileEvent.class);
        BDDMockito.then(streamBridge).should()
                .send(eq("monitored-profile-out-0"), captor.capture());

        MonitoredProfileEvent event = captor.getValue();
        then(event.getOwnerIgId()).isEqualTo("owner_123");
        then(event.getCompetitorIgHandle()).isEqualTo("rywal_pl");
        then(event.getActive()).isTrue();
        then(event.getChangeType())
                .isEqualTo(pl.autopilot.common.event.ChangeType.ADDED);
        then(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldMapRemovedChangeType() {
        // given
        MonitoredProfile profile = MonitoredProfile.builder()
                .ownerIgId("owner_123")
                .competitorIgHandle("rywal_pl")
                .active(false)
                .build();

        given(streamBridge.send(eq("monitored-profile-out-0"), any())).willReturn(true);

        // when
        adapter.publish(profile, ChangeType.REMOVED);

        // then
        ArgumentCaptor<MonitoredProfileEvent> captor =
                ArgumentCaptor.forClass(MonitoredProfileEvent.class);
        BDDMockito.then(streamBridge).should()
                .send(eq("monitored-profile-out-0"), captor.capture());

        then(captor.getValue().getChangeType())
                .isEqualTo(pl.autopilot.common.event.ChangeType.REMOVED);
        then(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldNotThrowWhenSendReturnsFalse() {
        // given
        MonitoredProfile profile = MonitoredProfile.builder()
                .ownerIgId("owner_123")
                .competitorIgHandle("rywal_pl")
                .build();

        given(streamBridge.send(eq("monitored-profile-out-0"), any())).willReturn(false);

        // when / then — brak wyjątku, tylko log błędu
        adapter.publish(profile, ChangeType.ADDED);
    }
}