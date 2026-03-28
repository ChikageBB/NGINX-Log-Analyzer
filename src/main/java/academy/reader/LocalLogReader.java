package academy.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalLogReader implements LogReader {

    private final String path;

    public LocalLogReader(final String path) {
        this.path = path;
    }

    @Override
    public Stream<String> readLines() {
        Path filePath = Path.of(path);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException(String.format("Log file %s does not exist", filePath));
        }

        if (!isSupportedFormat(path)) {
            throw new IllegalArgumentException(String.format("Log file %s is not supported", path));
        }

        try {
            return Files.lines(filePath);
        } catch (IOException e) {
            log.warn("Error reading log file {}", filePath, e);
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    private boolean isSupportedFormat(String path) {
        return path.endsWith(".log") || path.endsWith(".txt");
    }
}
