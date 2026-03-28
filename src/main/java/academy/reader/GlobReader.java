package academy.reader;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobReader implements LogReader {

    private final String pattern;
    private final List<Path> matchedFiles = new ArrayList<>();

    public GlobReader(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Stream<String> readLines() {
        try {
            List<Path> matchingFiles = findMatchingFiles();

            if (matchingFiles.isEmpty()) {
                log.warn("No files matching " + pattern);
                throw new IllegalArgumentException("No files matching pattern : " + pattern);
            }

            log.info("Found {} files matching pattern: {}", matchingFiles.size(), pattern);
            matchedFiles.addAll(matchingFiles);

            return matchingFiles.stream()
                    .peek(file -> log.info("Reading file: {}", file))
                    .flatMap(this::readFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getMatchedFiles() {
        return matchedFiles.stream().map(Path::getFileName).map(Path::toString).toList();
    }

    private List<Path> findMatchingFiles() {
        Path patternPath = Path.of(pattern);
        Path parentDir = patternPath.getParent();
        String fileName = patternPath.getFileName().toString();

        if (parentDir == null) {
            parentDir = Path.of(".");
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileName);

        List<Path> matchingFiles = new ArrayList<>();

        if (Files.isDirectory(parentDir)) {
            try (Stream<Path> paths = Files.list(parentDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .sorted()
                        .forEach(matchingFiles::add);
            } catch (IOException e) {
                log.error("Failed to list contents of {}", parentDir, e);
                throw new RuntimeException(e);
            }
        }

        return matchingFiles;
    }

    private Stream<String> readFile(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            log.warn("Failed to read file: {}", path, e);
            return Stream.empty();
        }
    }
}
