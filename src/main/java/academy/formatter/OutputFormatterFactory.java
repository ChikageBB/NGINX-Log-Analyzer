package academy.formatter;

public class OutputFormatterFactory {

    public OutputFormatter create(String format) {
        return switch (format.toLowerCase()) {
            case "json" -> new JsonFormatter();
            case "markdown" -> new MarkdownFormatter();
            case "adoc" -> new AdocFormatter();
            default -> throw new IllegalArgumentException(String.format("Unknown format: %s", format));
        };
    }
}
