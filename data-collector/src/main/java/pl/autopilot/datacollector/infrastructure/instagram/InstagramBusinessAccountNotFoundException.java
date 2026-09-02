package pl.autopilot.datacollector.infrastructure.instagram;

public class InstagramBusinessAccountNotFoundException extends RuntimeException {
    public InstagramBusinessAccountNotFoundException(String ownerId) {
        super("Konto biznesowe IG nie znalezione dla ownerId=" + ownerId);
    }

}
