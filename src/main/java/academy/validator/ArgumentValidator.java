package academy.validator;

import academy.cli.CliArguments;
import academy.model.Format;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArgumentValidator {

    public void validate(CliArguments args) {
        log.info("Validating format...");
        validateFormat(args.getFormat());
        log.info("Validating output...");
        validateOutput(args.getOutput(), args.getFormat());
        log.info("Validating date range...");
        validateDateRange(args.getFromDate(), args.getToDate());
    }

    private void validateFormat(String format) {
        if (format == null) {
            throw new IllegalArgumentException("Format must be specified");
        }

        try {
            Format.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private void validateOutput(String output, String format) {
        if (output == null || output.isEmpty()) {
            log.error("Output is null or empty");
            throw new IllegalArgumentException("Output path is required");
        }
        log.info("Output path: {}", output);

        Path path = Path.of(output);

        if (Files.exists(path)) {
            log.error("Output '{}' already exists", output);
            throw new IllegalArgumentException(String.format("Output file does not exist: %s", output));
        }

        Format actualFormat = Format.valueOf(format.toUpperCase());
        String expectedExtension =
                switch (actualFormat) {
                    case MARKDOWN -> ".md";
                    case JSON -> ".json";
                    case ADOC -> ".ad";
                };

        if (!output.endsWith(expectedExtension)) {
            log.error("Output file does not end with '.md', '.json' or '.ad' extension");
            throw new IllegalArgumentException(
                    String.format("Output file does not end with expected format: %s", expectedExtension));
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(String.format("Invalid date range: %s - %s", from, to));
        }
    }
}
