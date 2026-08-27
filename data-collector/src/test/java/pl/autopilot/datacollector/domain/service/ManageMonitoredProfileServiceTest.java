package pl.autopilot.datacollector.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.autopilot.datacollector.domain.model.ChangeType;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfileEventPort;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ManageMonitoredProfileServiceTest {

    @Mock
    private MonitoredProfilePort      monitoredProfilePort;
    @Mock
    private MonitoredProfileEventPort monitoredProfileEventPort;

    @InjectMocks
    private ManageMonitoredProfileService service;

    private static final String OWNER_IG_ID = "owner_123";
    private static final String HANDLE      = "rywal_pl";

    // ── addProfile — nowy profil ─────────────────────────────────────────────

    @Test
    void shouldCreateNewProfileWhenNotExists() {
        // given
        given(monitoredProfilePort.findByOwnerIgIdAndPlatformAndHandle(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE))
                .willReturn(Optional.empty());

        // when
        MonitoredProfile result = service.addProfile(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE);

        // then
        then(result.getOwnerIgId()).isEqualTo(OWNER_IG_ID);
        then(result.getCompetitorIgHandle()).isEqualTo(HANDLE);
        then(result.isActive()).isTrue();
        BDDMockito.then(monitoredProfilePort).should().save(result);
    }

    @Test
    void shouldPublishAddedEventForNewProfile() {
        // given
        given(monitoredProfilePort.findByOwnerIgIdAndPlatformAndHandle(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE))
                .willReturn(Optional.empty());

        // when
        MonitoredProfile result = service.addProfile(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE);

        // then
        BDDMockito.then(monitoredProfileEventPort).should()
                .publish(result, ChangeType.ADDED);
    }

    // ── addProfile — reaktywacja istniejącego ────────────────────────────────

    @Test
    void shouldReactivateExistingInactiveProfile() {
        // given
        MonitoredProfile inactive = MonitoredProfile.builder()
                .id(UUID.randomUUID())
                .ownerIgId(OWNER_IG_ID)
                .competitorIgHandle(HANDLE)
                .active(false)
                .build();

        given(monitoredProfilePort.findByOwnerIgIdAndPlatformAndHandle(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE))
                .willReturn(Optional.of(inactive));

        // when
        MonitoredProfile result = service.addProfile(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE);

        // then
        then(result.isActive()).isTrue();
        then(result.getId()).isEqualTo(inactive.getId());
        BDDMockito.then(monitoredProfileEventPort).should()
                .publish(result, ChangeType.ADDED);
    }

    // ── deactivateProfile ─────────────────────────────────────────────────────

    @Test
    void shouldDeactivateExistingProfile() {
        // given
        MonitoredProfile active = MonitoredProfile.builder()
                .id(UUID.randomUUID())
                .ownerIgId(OWNER_IG_ID)
                .competitorIgHandle(HANDLE)
                .active(true)
                .build();

        given(monitoredProfilePort.findByOwnerIgIdAndPlatformAndHandle(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE))
                .willReturn(Optional.of(active));

        // when
        service.deactivateProfile(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE);

        // then
        ArgumentCaptor<MonitoredProfile> captor =
                ArgumentCaptor.forClass(MonitoredProfile.class);
        BDDMockito.then(monitoredProfilePort).should().save(captor.capture());
        then(captor.getValue().isActive()).isFalse();

        BDDMockito.then(monitoredProfileEventPort).should()
                .publish(captor.getValue(), ChangeType.REMOVED);
    }

    @Test
    void shouldThrowWhenDeactivatingNonExistentProfile() {
        // given
        given(monitoredProfilePort.findByOwnerIgIdAndPlatformAndHandle(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE))
                .willReturn(Optional.empty());

        // when / then
        thenThrownBy(() -> service.deactivateProfile(OWNER_IG_ID, SocialMediaPlatform.INSTAGRAM, HANDLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(OWNER_IG_ID)
                .hasMessageContaining(HANDLE);

        BDDMockito.then(monitoredProfileEventPort).shouldHaveNoInteractions();
    }
}