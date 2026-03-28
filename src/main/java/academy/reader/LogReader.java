package academy.reader;

import java.util.stream.Stream;

public interface LogReader {
    Stream<String> readLines();
}
