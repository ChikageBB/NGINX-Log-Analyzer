package academy.services;

import academy.cli.CliArguments;
import academy.formatter.OutputFormatter;
import academy.formatter.OutputFormatterFactory;
import academy.model.LogEntry;
import academy.model.Statistics;
import academy.parser.LogParser;
import academy.reader.GlobReader;
import academy.reader.LogReader;
import academy.reader.LogReaderFactory;
import academy.statistics.StatisticsCalculator;
import academy.validator.ArgumentValidator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogAnalyzerService {

    private final CliArguments arguments;
    private final ArgumentValidator validator;
    private final LogReaderFactory readerFactory;
    private final LogParser parser;
    private final StatisticsCalculator calculator;
    private final OutputFormatterFactory formatterFactory;

    private final List<String> actualProcessedFiles = new ArrayList<>();

    public LogAnalyzerService(
            CliArguments arguments,
            ArgumentValidator validator,
            LogReaderFactory readerFactory,
            LogParser parser,
            StatisticsCalculator calculator,
            OutputFormatterFactory formatterFactory) {
        this.arguments = arguments;
        this.validator = validator;
        this.readerFactory = readerFactory;
        this.parser = parser;
        this.calculator = calculator;
        this.formatterFactory = formatterFactory;
    }

    public void analyze() {
        log.info("Step 1: Validating logs...");
        validator.validate(arguments);

        log.info("Step 2: Reading and parsing logs...");
        List<LogEntry> entries = readAndParse();
        log.info("Step 3: Filtering logs by date...");
        Stream<LogEntry> filtered = filterByDateRange(entries.stream());

        log.info("Step 4: Calculating statistics...");
        Statistics statistics = calculator.calculateStatistics(filtered, actualProcessedFiles);

        log.info("Step 5: Writing output to {}", arguments.getOutput());
        OutputFormatter outputFormatter = formatterFactory.create(arguments.getFormat());
        outputFormatter.write(statistics, arguments.getOutput());

        log.info("Statistics saved to: {}", arguments.getOutput());
    }

    private List<LogEntry> readAndParse() {
        List<LogEntry> allEntries = new ArrayList<>();

        for (String path : arguments.getPaths()) {
            log.info("Reading log from {}", path);
            LogReader reader = readerFactory.create(path);

            if (reader instanceof GlobReader globReader) {

                globReader.readLines().forEach(line -> {
                    try {
                        parser.parse(line).ifPresent(allEntries::add);
                    } catch (Exception e) {
                        log.warn("Error while parsing line", e);
                    }
                });

                List<String> matchedFiles = globReader.getMatchedFiles();
                if (matchedFiles.isEmpty()) {
                    actualProcessedFiles.add(path);
                } else {
                    actualProcessedFiles.addAll(matchedFiles);
                }
                log.info("Matched files: {}", matchedFiles);

            } else {
                String fileName = Path.of(path).getFileName().toString();
                actualProcessedFiles.add(fileName);

                reader.readLines().forEach(line -> {
                    try {
                        parser.parse(line).ifPresent(allEntries::add);
                    } catch (Exception e) {
                        log.warn("Error while parsing line", e);
                    }
                });
            }
        }
        return allEntries;
    }

    private Stream<LogEntry> filterByDateRange(Stream<LogEntry> entries) {
        var from = arguments.getFromDate();
        var to = arguments.getToDate();

        if (from == null && to == null) {
            return entries;
        }

        return entries.filter(entry -> {
            var date = entry.timestamp().toLocalDate();
            boolean afterFrom = from == null || !date.isBefore(from);
            boolean beforeTo = to == null || !date.isAfter(to);
            return afterFrom && beforeTo;
        });
    }
}
