package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.formatter.AdocFormatter;
import academy.formatter.JsonFormatter;
import academy.formatter.MarkdownFormatter;
import academy.model.DateStat;
import academy.model.ResourceStat;
import academy.model.ResponseCodeStats;
import academy.model.ResponseSizeStats;
import academy.model.Statistics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StatisticsReportTest {

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new Statistics(
                List.of("access.log", "error.log"),
                150,
                new ResponseSizeStats(500, 1200, 900),
                List.of(new ResourceStat("/index.html", 80), new ResourceStat("/api/v1/users", 70)),
                List.of(new ResponseCodeStats(200, 120), new ResponseCodeStats(404, 30)),
                List.of(
                        new DateStat("2024-01-10", "Wednesday", 100, 66.6),
                        new DateStat("2024-01-11", "Thursday", 50, 33.3)),
                List.of("HTTP/1.1", "HTTP/2"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest() throws IOException {
        JsonFormatter jsonFormatter = new JsonFormatter();

        Path file = Files.createTempFile("stats", ".json");
        jsonFormatter.write(statistics, file.toString());

        String json = Files.readString(file);
        assertTrue(json.contains("\"files\""));
        assertTrue(json.contains("\"totalRequestsCount\""));
        assertTrue(json.contains("\"resources\""));
        assertTrue(json.contains("\"responseCodes\""));
        assertTrue(json.contains("\"requestsPerDate\""));
        assertTrue(json.contains("\"uniqueProtocols\""));
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() throws IOException {
        MarkdownFormatter markdownFormatter = new MarkdownFormatter();

        Path file = Files.createTempFile("stats", ".markdown");
        markdownFormatter.write(statistics, file.toString());

        String markdown = Files.readString(file);

        assertTrue(markdown.contains("#### Общая информация"));
        assertTrue(markdown.contains("Количество запросов"));
        assertTrue(markdown.contains("/index.html"));
        assertTrue(markdown.contains("404"));
        assertTrue(markdown.contains("Уникальные протоколы"));
        assertTrue(markdown.contains("HTTP/1.1"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() throws IOException {
        AdocFormatter formatter = new AdocFormatter();

        Path file = Files.createTempFile("stats", ".adoc");
        formatter.write(statistics, file.toString());

        String adoc = Files.readString(file);

        assertTrue(adoc.contains("= NGINX Log Analysis Report"));
        assertTrue(adoc.contains("== Общая информация"));
        assertTrue(adoc.contains("`access.log, error.log`"));
        assertTrue(adoc.contains("/api/v1/users"));
        assertTrue(adoc.contains("== Коды ответа"));
        assertTrue(adoc.contains("404"));
        assertTrue(adoc.contains("HTTP/1.1"));
    }
}
