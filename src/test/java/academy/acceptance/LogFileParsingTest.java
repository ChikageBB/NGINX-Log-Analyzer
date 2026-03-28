package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cli.CliArguments;
import academy.formatter.OutputFormatterFactory;
import academy.parser.NginxParser;
import academy.reader.LocalLogReader;
import academy.reader.LogReaderFactory;
import academy.reader.RemoteLogReader;
import academy.services.LogAnalyzerService;
import academy.statistics.StatisticsCalculator;
import academy.validator.ArgumentValidator;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogFileParsingTest {

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
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest(@TempDir Path tempDir) throws IOException {
        Path log = tempDir.resolve("log.txt");
        Files.writeString(log, "line1\nline2\nline3\n");

        LocalLogReader reader = new LocalLogReader(log.toString());

        List<String> lines = reader.readLines().toList();

        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest(@TempDir Path tempDir) throws IOException {
        // Поднимаем HTTP сервер на свободном порту
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        String logData =
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] " + "\"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" "
                        + "\"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        server.createContext("/remote.txt", exchange -> {
            byte[] bytes = logData.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        server.start();

        String url = "http://localhost:" + server.getAddress().getPort() + "/remote.txt";

        RemoteLogReader reader = new RemoteLogReader(url);

        List<String> lines = reader.readLines().toList();

        assertEquals(1, lines.size());
        server.stop(0);
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл, "
            + "часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.write(
                logFile,
                List.of(
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 1000 \"-\" \"Mozilla/5.0\"",
                        "93.180.71.3 - - [18/May/2015:08:05:32 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 1500 \"-\" \"Mozilla/5.0\"",
                        "93.180.71.3 - - [19/May/2015:08:05:32 +0000] \"GET /downloads/product_3 HTTP/1.1\" 200 2000 \"-\" \"Mozilla/5.0\""));

        Path outputFile = tempDir.resolve("output.md");

        // Создаём CliArguments напрямую
        CliArguments args = new CliArguments();
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(outputFile.toString());
        args.setFormat("markdown");
        args.setFromDate(LocalDate.parse("2015-05-18")); // фильтруем от 18 мая
        args.setToDate(LocalDate.parse("2015-05-18")); // до 18 мая включительно

        var service = createService(args);
        service.analyze();

        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("/downloads/product_2"));
        assertFalse(content.contains("/downloads/product_1"));
        assertFalse(content.contains("/downloads/product_3"));
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.write(
                logFile,
                List.of(
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 1000 \"-\" \"Mozilla/5.0\"",
                        "this is a corrupted line that cannot be parsed",
                        "93.180.71.3 - - [19/May/2015:08:05:32 +0000] \"GET /downloads/product_3 HTTP/1.1\" 200 2000 \"-\" \"Mozilla/5.0\""));

        Path outputFile = tempDir.resolve("output.md");

        CliArguments args = new CliArguments();
        args.setPaths(List.of(logFile.toString()));
        args.setOutput(outputFile.toString());
        args.setFormat("markdown");

        var logAnalyzerService = createService(args);

        assertDoesNotThrow(logAnalyzerService::analyze);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("/downloads/product_1"));
        assertTrue(content.contains("/downloads/product_3"));
        assertFalse(content.contains("corrupted line"));
    }
}
