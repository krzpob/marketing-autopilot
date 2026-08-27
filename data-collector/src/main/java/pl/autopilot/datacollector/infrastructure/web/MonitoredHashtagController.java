package pl.autopilot.datacollector.infrastructure.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;
import pl.autopilot.datacollector.domain.model.MonitoredHashtag;
import pl.autopilot.datacollector.domain.model.SocialMediaPlatform;
import pl.autopilot.datacollector.domain.port.out.MonitoredHashtagPort;
import pl.autopilot.datacollector.domain.service.ManageMonitoredHashtagService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photographers/{ownerIgId}/monitored-hashtags")
public class MonitoredHashtagController {

    private final ManageMonitoredHashtagService manageMonitoredHashtagService;
    private final MonitoredHashtagPort monitoredHashtagPort;

    @PostMapping()
    public MonitoredHashtag postMethodName(@RequestBody MonitoredHashtagRequest request, @PathVariable String ownerIgId) {
        SocialMediaPlatform platform = request.platform() != null
                ? SocialMediaPlatform.valueOf(request.platform())
                : SocialMediaPlatform.INSTAGRAM;
        return manageMonitoredHashtagService.addHashtag(ownerIgId, platform, request.hashtag());
    }

    @GetMapping()
    public List<MonitoredHashtag> getMethodName(@PathVariable String ownerIgId) {
        return monitoredHashtagPort.findAllByOwnerIgId(ownerIgId).stream()
                .filter(MonitoredHashtag::isActive)
                .toList();
    }

    @DeleteMapping("/{hashtag}")
    public void deactivateHashtag(@PathVariable String ownerIgId, @PathVariable String hashtag, @RequestParam(required = false) SocialMediaPlatform platform) {
        if (platform != null) {
            manageMonitoredHashtagService.deactivateHashtag(ownerIgId, platform, hashtag);
        } else {
            manageMonitoredHashtagService.deactivateHashtagOnAllPlatforms(ownerIgId, hashtag);
        }
    }
    
    record MonitoredHashtagRequest(String hashtag, String platform) {}

}
