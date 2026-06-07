package pl.autopilot.datacollector.infrastructure.instagram.client;

import pl.autopilot.datacollector.infrastructure.instagram.model.InstagramErrorResponse;

public class InstagramApiException extends RuntimeException {

    private final int code;
    private final String type;
    private final String fbtraceId;

    public InstagramApiException(InstagramErrorResponse.ErrorDetail error) {
        super("[%d] %s (type=%s, fbtrace=%s)"
                .formatted(error.getCode(), error.getMessage(),
                           error.getType(), error.getFbtraceId()));
        this.code      = error.getCode();
        this.type      = error.getType();
        this.fbtraceId = error.getFbtraceId();
    }

    public int getCode()        { return code; }
    public String getType()     { return type; }
    public String getFbtraceId(){ return fbtraceId; }

    /** Token wygasł lub jest nieprawidłowy — kod 190 */
    public boolean isTokenExpired() {
        return code == 190;
    }

    /** Przekroczono limit zapytań — kod 4 lub 17 */
    public boolean isRateLimited() {
        return code == 4 || code == 17;
    }
}