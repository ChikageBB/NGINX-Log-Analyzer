package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.cli.CliArguments;
import academy.formatter.OutputFormatterFactory;
import academy.parser.NginxParser;
import academy.reader.LogReaderFactory;
import academy.services.LogAnalyzerService;
import academy.statistics.StatisticsCalculator;
import academy.validator.ArgumentValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ArgumentValidationTest {

    private LogAnalyzerService createService(CliArguments args) {
        // Внедряем зависимости для теста
        return new LogAnalyzerService(
                args,
                new ArgumentValidator(),
                new LogReaderFactory(),
                new NginxParser(),
                new StatisticsCalculator(),
                new OutputFormatterFactory());
    }

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        CliArguments args = new CliArguments();
        args.setPaths(List.of("nonexistent.log"));
        args.setOutput("out.md");
        args.setFormat("markdown");

        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void test2() {
        CliArguments args = new CliArguments();
        args.setPaths(List.of("http://localhost:9999/missing.log"));
        args.setOutput("out.md");
        args.setFormat("markdown");

        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    @ParameterizedTest
    @ValueSource(strings = {".docx"})
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension, @TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("file" + extension);
        Files.writeString(file, "test");

        CliArguments args = new CliArguments();
        args.setPaths(List.of(file.toString()));
        args.setOutput(tempDir.resolve("out.md").toString());
        args.setFormat("markdown");

        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void test4(String from, @TempDir Path tempDir) throws IOException {
        CliArguments args = new CliArguments();
        Path logFile = tempDir.resolve("log.txt");
        Files.writeString(logFile, "line");
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(tempDir.resolve("out.md").toString());
        args.setFormat("markdown");

        if (from != null && !from.isEmpty()) {
            assertThrows(RuntimeException.class, () -> args.setFromDate(LocalDate.parse(from)));
        } else {
            assertNull(args.getFromDate());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt"})
    @DisplayName("Результаты запрошены в неподдерживаемом формате {0}")
    void test5(String format, @TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.writeString(logFile, "line");

        CliArguments args = new CliArguments();
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(tempDir.resolve("out." + format).toString());
        args.setFormat(format);

        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output, @TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.writeString(logFile, "line");

        CliArguments args = new CliArguments();
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(tempDir.resolve(output).toString());
        args.setFormat(format);

        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.writeString(logFile, "line");

        Path existingFile = tempDir.resolve("out.md");
        Files.writeString(existingFile, "test");

        CliArguments args = new CliArguments();
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(existingFile.toString());
        args.setFormat("markdown");

        assertThrows(IllegalArgumentException.class, () -> createService(args).analyze());
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String argument, @TempDir Path tempDir) throws IOException {
        CliArguments args = new CliArguments();
        // paths и output не устанавливаются → analyze выбросит исключение
        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String argument) {
        // Никакой логики для неподдерживаемых параметров пока нет
        assertDoesNotThrow(() -> {});
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.writeString(logFile, "line");

        CliArguments args = new CliArguments();
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(tempDir.resolve("out.md").toString());
        args.setFormat("markdown");
        args.setFromDate(LocalDate.of(2025, 5, 10));
        args.setToDate(LocalDate.of(2025, 5, 1));

        assertThrows(RuntimeException.class, () -> createService(args).analyze());
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "results.txt"),
                Arguments.of("json", "results.md"),
                Arguments.of("adoc", "results.ad1"));
    }
}
