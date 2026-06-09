package pl.autopilot.datacollector.infrastructure.instagram.client;

public class InstagramTokenExpiredException extends RuntimeException {

    private final String ownerIgId;

    public InstagramTokenExpiredException(String ownerIgId) {
        super("Token wygasł dla ownerIgId: " + ownerIgId);
        this.ownerIgId = ownerIgId;
    }

    public String getOwnerIgId() { return ownerIgId; }
}