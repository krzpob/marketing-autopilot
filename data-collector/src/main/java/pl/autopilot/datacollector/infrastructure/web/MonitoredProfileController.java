package pl.autopilot.datacollector.infrastructure.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pl.autopilot.datacollector.domain.model.MonitoredProfile;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.MonitoredProfilePort;
import pl.autopilot.datacollector.domain.service.ManageMonitoredProfileService;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photographers/{ownerIgId}/monitored-profiles")
public class MonitoredProfileController {

    private final ManageMonitoredProfileService manageMonitoredProfileService;
    private final MonitoredProfilePort monitoredProfilePort;

    @PostMapping()
    public MonitoredProfile postMethodName(@RequestBody MonitoredProfileRequest request, @PathVariable String ownerIgId) {
        if (!Arrays.asList(SocialMediaPlatform.values()).contains(SocialMediaPlatform.valueOf(request.platform()))) {
            throw new IllegalArgumentException("Invalid social media platform");
        }
        return manageMonitoredProfileService.addProfile(ownerIgId, 
            SocialMediaPlatform.valueOf(request.platform()), request.competitorHandle());
    }

    @GetMapping()
    public List<MonitoredProfile> getMethodName(@PathVariable String ownerIgId) {
        return monitoredProfilePort.findAllByOwnerIgId(ownerIgId).stream()
                .filter(MonitoredProfile::isActive)
                .map(profile -> profile.toBuilder().platform(profile.getPlatform()).build())
                .toList();
    }
    
    @DeleteMapping("/{competitorHandle}")
    public void deactivateProfile(@PathVariable String ownerIgId, @PathVariable String competitorHandle,@RequestParam(required = false) SocialMediaPlatform platform) {
        if (platform != null) {
            manageMonitoredProfileService.deactivateProfile(ownerIgId, platform, competitorHandle);
        } else {
            manageMonitoredProfileService.deactivateProfileOnAllPlatforms(ownerIgId, competitorHandle);
        }
    }

    record MonitoredProfileRequest( String competitorHandle, String platform) {}
}

