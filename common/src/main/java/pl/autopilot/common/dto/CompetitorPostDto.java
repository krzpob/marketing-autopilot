package pl.autopilot.common.dto;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import pl.autopilot.common.dto.CompetitorPostDto.MediaType;

public record CompetitorPostDto(

    // ── Identyfikacja ─────────────────────────────────────────────
    String id,                      // Instagram media ID
    String shortcode,               // instagram.com/p/{shortcode}
    String ownerIgId,               // Instagram user ID właściciela
    String ownerUsername,           // @username

    // ── Typ contentu ──────────────────────────────────────────────
    MediaType mediaType,            // IMAGE / VIDEO / CAROUSEL_ALBUM / REEL

    // ── Treść ─────────────────────────────────────────────────────
    String caption,                 // pełny tekst posta
    List<String> hashtags,          // wyekstrahowane z caption, bez '#'
    List<String> mentions,          // wyekstrahowane z caption, bez '@'
    String mediaUrl,                // URL do zdjęcia / miniaturki
    String permalink,               // pełny URL do posta na IG

    // ── Metryki zaangażowania ─────────────────────────────────────
    long likeCount,
    int commentsCount,
    int shareCount,                 // dostępny tylko dla własnego konta (Insights API)

    // ── Snapshot profilu w momencie pobrania ──────────────────────
    // Potrzebny do EngagementRateCalculator — ER = (likes+comments)/followers
    // Nie trzymamy live wartości, bo followersi zmieniają się w czasie
    long ownerFollowerCount,
    int ownerMediaCount,            // ile postów ma konto — miara aktywności

    // ── Czas ──────────────────────────────────────────────────────
    Instant publishedAt,            // kiedy post został opublikowany (z IG API)
    Instant collectedAt             // kiedy my go pobraliśmy (ustawia Data Collector)

) {
    public enum MediaType {
        IMAGE,
        VIDEO,
        CAROUSEL_ALBUM,
        REEL
    }

    // Convenience method — nie trzymamy ER jako pola, bo zależy
    // od followerCount który może być różny przy każdym pobraniu
    public double engagementRate() {
        if (ownerFollowerCount == 0) return 0.0;
        return (double) (likeCount + commentsCount) / ownerFollowerCount * 100;
    }

    // Walidacja przy konstrukcji (compact constructor)
    public CompetitorPostDto {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(ownerIgId, "ownerIgId is required");
        Objects.requireNonNull(mediaType, "mediaType is required");
        Objects.requireNonNull(publishedAt, "publishedAt is required");
        Objects.requireNonNull(collectedAt, "collectedAt is required");
        hashtags  = hashtags  != null ? List.copyOf(hashtags)  : List.of();
        mentions  = mentions  != null ? List.copyOf(mentions)   : List.of();
    }
}