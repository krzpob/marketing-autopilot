package pl.autopilot.datacollector.domain.port.in;

public interface RefreshAccessTokenUseCase {

    void refreshToken(String ownerIgId);

    /** Wywoływane przez scheduler — odświeża wszystkie tokeny bliskie wygaśnięcia */
    void refreshAllExpiring();
}