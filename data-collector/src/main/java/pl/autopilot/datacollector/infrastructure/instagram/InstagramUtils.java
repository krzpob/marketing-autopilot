package pl.autopilot.datacollector.infrastructure.instagram;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@Slf4j
public final class InstagramUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .appendOffset("+HHMM", "Z")
                    .toFormatter();

    private InstagramUtils() {}

    public static Instant parseTimestamp(String timestamp) {
        if (timestamp == null) return Instant.now();
        try {
            return Instant.from(TIMESTAMP_FORMATTER.parse(timestamp));
        } catch (DateTimeParseException e) {
            log.warn("Nie można sparsować timestamp: {}", timestamp);
            return Instant.now();
        }
    }
}