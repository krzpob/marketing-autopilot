package pl.autopilot.datacollector.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ManageMonitoredHashtagServiceTest {

    @Mock
    private MonitoredHashtagPort      monitoredHashtagPort;
    @Mock
    private MonitoredHashtagEventPort monitoredHashtagEventPort;

    @InjectMocks
    private ManageMonitoredHashtagService service;

    private static final String OWNER_IG_ID = "owner_123";
    private static final String HASHTAG     = "fotografia";

    // ── addHashtag — nowy ─────────────────────────────────────────────────────

    @Test
    void shouldCreateNewHashtagWhenNotExists() {
        // given
        given(monitoredHashtagPort.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.empty());

        // when
        MonitoredHashtag result = service.addHashtag(OWNER_IG_ID, HASHTAG);

        // then
        then(result.getOwnerIgId()).isEqualTo(OWNER_IG_ID);
        then(result.getHashtag()).isEqualTo(HASHTAG);
        then(result.isActive()).isTrue();
        BDDMockito.then(monitoredHashtagPort).should().save(result);
    }

    @Test
    void shouldPublishAddedEventForNewHashtag() {
        // given
        given(monitoredHashtagPort.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.empty());

        // when
        MonitoredHashtag result = service.addHashtag(OWNER_IG_ID, HASHTAG);

        // then
        BDDMockito.then(monitoredHashtagEventPort).should()
                .publish(result, ChangeType.ADDED);
    }

    // ── addHashtag — reaktywacja ──────────────────────────────────────────────

    @Test
    void shouldReactivateExistingInactiveHashtag() {
        // given
        MonitoredHashtag inactive = MonitoredHashtag.builder()
                .id(UUID.randomUUID())
                .ownerIgId(OWNER_IG_ID)
                .hashtag(HASHTAG)
                .active(false)
                .build();

        given(monitoredHashtagPort.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.of(inactive));

        // when
        MonitoredHashtag result = service.addHashtag(OWNER_IG_ID, HASHTAG);

        // then
        then(result.isActive()).isTrue();
        then(result.getId()).isEqualTo(inactive.getId());
        BDDMockito.then(monitoredHashtagEventPort).should()
                .publish(result, ChangeType.ADDED);
    }

    // ── deactivateHashtag ─────────────────────────────────────────────────────

    @Test
    void shouldDeactivateExistingHashtag() {
        // given
        MonitoredHashtag active = MonitoredHashtag.builder()
                .id(UUID.randomUUID())
                .ownerIgId(OWNER_IG_ID)
                .hashtag(HASHTAG)
                .active(true)
                .build();

        given(monitoredHashtagPort.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.of(active));

        // when
        service.deactivateHashtag(OWNER_IG_ID, HASHTAG);

        // then
        ArgumentCaptor<MonitoredHashtag> captor =
                ArgumentCaptor.forClass(MonitoredHashtag.class);
        BDDMockito.then(monitoredHashtagPort).should().save(captor.capture());
        then(captor.getValue().isActive()).isFalse();

        BDDMockito.then(monitoredHashtagEventPort).should()
                .publish(captor.getValue(), ChangeType.REMOVED);
    }

    @Test
    void shouldThrowWhenDeactivatingNonExistentHashtag() {
        // given
        given(monitoredHashtagPort.findByOwnerIgIdAndHashtag(OWNER_IG_ID, HASHTAG))
                .willReturn(Optional.empty());

        // when / then
        thenThrownBy(() -> service.deactivateHashtag(OWNER_IG_ID, HASHTAG))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(OWNER_IG_ID)
                .hasMessageContaining(HASHTAG);

        BDDMockito.then(monitoredHashtagEventPort).shouldHaveNoInteractions();
    }
}