package academy.reader;

public class LogReaderFactory {
    public LogReader create(String path) {
        if (path.startsWith("https://") || path.startsWith("http://")) {
            return new RemoteLogReader(path);
        }

        if (path.contains("*")) {
            return new GlobReader(path);
        }

        return new LocalLogReader(path);
    }
}
