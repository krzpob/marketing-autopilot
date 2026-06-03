package pl.autopilot.datacollector.infrastructure.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/meta")
public class MetaCallbackController {

    /**
     * Wymagany przez Meta — endpoint do usuwania danych użytkownika.
     * URL konfigurowany w: Meta Developer Portal → App Settings → Advanced
     * → Data Deletion Request URL
     */
    @PostMapping("/data-deletion")
    public ResponseEntity<Map<String, String>> dataDeletion(
            @RequestBody(required = false) Map<String, Object> payload) {

        log.info("Otrzymano żądanie usunięcia danych od Meta: {}", payload);

        // TODO: implementacja faktycznego usunięcia danych użytkownika z bazy
        // accessTokenPort.delete(userId);

        return ResponseEntity.ok(Map.of(
                "url",        "https://krzpob.github.io/marketing-autopilot/deletion-status",
                "confirmation_code", "autopilot-deletion-" + System.currentTimeMillis()
        ));
    }
}