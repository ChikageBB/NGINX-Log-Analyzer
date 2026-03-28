package academy.parser;

import academy.model.LogEntry;
import java.util.Optional;

public interface LogParser {
    Optional<LogEntry> parse(String line);
}
