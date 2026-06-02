package pl.autopilot.datacollector.infrastructure.web;

class MetaCallbackController {
    // Ten endpoint będzie odbierał callbacki z Meta (np. webhooki o nowych postach)
    // i przekazywał je do odpowiednich serwisów w Data Collectorze.
    // Implementacja zależy od tego, jakie dokładnie dane Meta nam wyśle i w jakim formacie.
    @PostMapping("/data-deletion")
    public Map<String, String> dataDeletion(@RequestBody Map<String, String> body) {
        // TODO: w Bloku 5 — tutaj usuniesz dane użytkownika z DB
        String confirmationCode = UUID.randomUUID().toString();
        log.info("Data deletion request received, confirmation: {}", confirmationCode);
        return Map.of(
            "url", "https://twoja-domena/deletion-status?id=" + confirmationCode,
            "confirmation_code", confirmationCode
        );
    }
}