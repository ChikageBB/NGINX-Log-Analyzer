package academy.parser;

import academy.model.LogEntry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NginxParser implements LogParser {

    private static final Pattern LOG_PATTERNS = Pattern.compile(
            "^(\\S+) - (\\S+) \\[([^\\]]+)\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+) \"([^\"]*)\" \"([^\"]*)\"$");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    @Override
    public Optional<LogEntry> parse(String line) {
        var matcher = LOG_PATTERNS.matcher(line);

        if (!matcher.matches()) {
            log.warn("Invalid log line: {}", line);
            return Optional.empty();
        }

        try {
            return Optional.of(LogEntry.builder()
                    .remoteAddress(matcher.group(1))
                    .remoteUser(matcher.group(2))
                    .timestamp(parseTimestamp(matcher.group(3)))
                    .method(matcher.group(4))
                    .resource(matcher.group(5))
                    .protocol(matcher.group(6))
                    .status(Integer.parseInt(matcher.group(7)))
                    .bodyBytesSent(Long.parseLong(matcher.group(8)))
                    .httpReferer(matcher.group(9))
                    .httpUserAgent(matcher.group(10))
                    .build());
        } catch (NumberFormatException e) {
            log.warn("Invalid log line: {}", line);
            return Optional.empty();
        }
    }

    private LocalDateTime parseTimestamp(String timestampString) {
        return OffsetDateTime.parse(timestampString, DATE_TIME_FORMATTER).toLocalDateTime();
    }
}
