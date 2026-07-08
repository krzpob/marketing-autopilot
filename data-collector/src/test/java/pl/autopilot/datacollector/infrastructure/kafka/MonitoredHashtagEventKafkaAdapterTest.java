package pl.autopilot.datacollector.infrastructure.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import pl.autopilot.common.event.MonitoredHashtagEvent;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MonitoredHashtagEventKafkaAdapterTest {

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private MonitoredHashtagEventKafkaAdapter adapter;

    @Test
    void shouldPublishEventWithCorrectFields() {
        // given
        MonitoredHashtag hashtag = MonitoredHashtag.builder()
                .ownerIgId("owner_123")
                .hashtag("fotografia")
                .active(true)
                .build();

        given(streamBridge.send(eq("monitored-hashtag-out-0"), any())).willReturn(true);

        // when
        adapter.publish(hashtag, ChangeType.ADDED);

        // then
        ArgumentCaptor<MonitoredHashtagEvent> captor =
                ArgumentCaptor.forClass(MonitoredHashtagEvent.class);
        BDDMockito.then(streamBridge).should()
                .send(eq("monitored-hashtag-out-0"), captor.capture());

        MonitoredHashtagEvent event = captor.getValue();
        then(event.getOwnerIgId()).isEqualTo("owner_123");
        then(event.getHashtag()).isEqualTo("fotografia");
        then(event.getActive()).isTrue();
        then(event.getChangeType())
                .isEqualTo(pl.autopilot.common.event.ChangeType.ADDED);
        then(event.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldMapRemovedChangeType() {
        // given
        MonitoredHashtag hashtag = MonitoredHashtag.builder()
                .ownerIgId("owner_123")
                .hashtag("fotografia")
                .active(false)
                .build();

        given(streamBridge.send(eq("monitored-hashtag-out-0"), any())).willReturn(true);

        // when
        adapter.publish(hashtag, ChangeType.REMOVED);

        // then
        ArgumentCaptor<MonitoredHashtagEvent> captor =
                ArgumentCaptor.forClass(MonitoredHashtagEvent.class);
        BDDMockito.then(streamBridge).should()
                .send(eq("monitored-hashtag-out-0"), captor.capture());

        then(captor.getValue().getChangeType())
                .isEqualTo(pl.autopilot.common.event.ChangeType.REMOVED);
        then(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldNotThrowWhenSendReturnsFalse() {
        // given
        MonitoredHashtag hashtag = MonitoredHashtag.builder()
                .ownerIgId("owner_123")
                .hashtag("fotografia")
                .build();

        given(streamBridge.send(eq("monitored-hashtag-out-0"), any())).willReturn(false);

        // when / then — brak wyjątku
        adapter.publish(hashtag, ChangeType.ADDED);
    }
}