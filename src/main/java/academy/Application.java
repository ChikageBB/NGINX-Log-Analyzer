package academy;

import academy.cli.CliArguments;
import academy.formatter.OutputFormatterFactory;
import academy.parser.NginxParser;
import academy.reader.LogReaderFactory;
import academy.services.LogAnalyzerService;
import academy.statistics.StatisticsCalculator;
import academy.validator.ArgumentValidator;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "log-analyzer", version = "1.0", mixinStandardHelpOptions = true)
@Slf4j
public class Application implements Runnable {

    @Option(
            names = {"-p", "--path"},
            required = true,
            description = "Path to log files",
            arity = "1..*")
    private List<String> paths;

    @Option(
            names = {"-f", "--format"},
            required = true,
            defaultValue = "markdown",
            description = "Output format: json, markdown, adoc")
    private String format = "markdown";

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "Output file path")
    private String output;

    @Option(
            names = {"--from"},
            description = "Start date (ISO8601)")
    private LocalDate from;

    @Option(
            names = {"--to"},
            description = "End date (ISO8601)")
    private LocalDate to;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            log.info("Starting log analysis");
            log.info("Paths: {}", paths);
            log.info("Output: {}", output);
            log.info("Format: {}", format);

            CliArguments cliArguments = new CliArguments();

            cliArguments.setPaths(paths);
            cliArguments.setOutput(output);
            cliArguments.setFormat(format);
            cliArguments.setFromDate(from);
            cliArguments.setToDate(to);

            ArgumentValidator validator = new ArgumentValidator();
            LogReaderFactory readerFactory = new LogReaderFactory();
            var parser = new NginxParser();
            var calculator = new StatisticsCalculator();
            var formatterFactory = new OutputFormatterFactory();

            var service = new LogAnalyzerService(
                    cliArguments, validator, readerFactory, parser, calculator, formatterFactory);

            service.analyze();

            log.info("Analysis completed successfully");
        } catch (Exception e) {
            log.error("Analysis failed", e);
            throw new RuntimeException(e);
        }
    }
}
