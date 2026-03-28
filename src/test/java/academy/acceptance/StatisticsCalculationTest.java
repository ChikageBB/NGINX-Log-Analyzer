package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.model.LogEntry;
import academy.model.ResourceStat;
import academy.model.ResponseCodeStats;
import academy.model.Statistics;
import academy.statistics.StatisticsCalculator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StatisticsCalculationTest {

    private StatisticsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new StatisticsCalculator();
    }

    @Test
    @DisplayName("Расчет статистики для пустого списка")
    void testCalculateStatisticsEmptyList() {
        Statistics statistics = calculator.calculateStatistics(Stream.empty(), List.of("test.log"));

        assertEquals(0, statistics.totalRequestsCount());
        assertEquals(0, statistics.responseSizeInBytes().average());
        assertTrue(statistics.resources().isEmpty());
        assertTrue(statistics.responseCodes().isEmpty());
    }

    @Test
    @DisplayName("Расчет общего количества запросов")
    void testCalculateTotalRequests() {
        List<LogEntry> entries = List.of(
                createLogEntry("/index.html", 200, 1024),
                createLogEntry("/about.html", 200, 2048),
                createLogEntry("/contact.html", 404, 512));

        Statistics stats = calculator.calculateStatistics(entries.stream(), List.of("test.log"));

        assertEquals(3, stats.totalRequestsCount());
    }

    @Test
    @DisplayName("Расчет среднего размера ответа")
    void testCalculateAverageResponseSize() {
        List<LogEntry> entries = List.of(
                createLogEntry("/page1", 200, 1000),
                createLogEntry("/page2", 200, 2000),
                createLogEntry("/page3", 200, 3000));

        Statistics stats = calculator.calculateStatistics(entries.stream(), List.of("test.log"));

        assertEquals(2000, stats.responseSizeInBytes().average());
        assertEquals(3000, stats.responseSizeInBytes().max());
    }

    @Test
    @DisplayName("Расчет 95-го перцентиля")
    void testCalculate95Percentile() {
        List<LogEntry> entries = List.of(
                createLogEntry("/1", 200, 100),
                createLogEntry("/2", 200, 200),
                createLogEntry("/3", 200, 300),
                createLogEntry("/4", 200, 400),
                createLogEntry("/5", 200, 500),
                createLogEntry("/6", 200, 600),
                createLogEntry("/7", 200, 700),
                createLogEntry("/8", 200, 800),
                createLogEntry("/9", 200, 900),
                createLogEntry("/10", 200, 1000));

        Statistics stats = calculator.calculateStatistics(entries.stream(), List.of("test.log"));

        assertEquals(955.0, stats.responseSizeInBytes().p95());
    }

    @Test
    @DisplayName("Топ-10 наиболее запрашиваемых ресурсов")
    void testTopResources() {
        List<LogEntry> entries = List.of(
                createLogEntry("/index.html", 200, 100),
                createLogEntry("/index.html", 200, 100),
                createLogEntry("/index.html", 200, 100),
                createLogEntry("/about.html", 200, 100),
                createLogEntry("/about.html", 200, 100),
                createLogEntry("/contact.html", 200, 100));

        Statistics stats = calculator.calculateStatistics(entries.stream(), List.of("test.log"));

        List<ResourceStat> resources = stats.resources();
        assertEquals(3, resources.size());
        assertEquals("/index.html", resources.get(0).resource());
        assertEquals(3, resources.get(0).totalRequestsCount());
        assertEquals("/about.html", resources.get(1).resource());
        assertEquals(2, resources.get(1).totalRequestsCount());
    }

    @Test
    @DisplayName("Подсчет кодов ответа")
    void testResponseCodes() {
        List<LogEntry> entries = List.of(
                createLogEntry("/page1", 200, 100),
                createLogEntry("/page2", 200, 100),
                createLogEntry("/page3", 404, 100),
                createLogEntry("/page4", 500, 100));

        Statistics stats = calculator.calculateStatistics(entries.stream(), List.of("test.log"));

        List<ResponseCodeStats> codes = stats.responseCodes();
        assertEquals(3, codes.size());

        ResponseCodeStats code200 =
                codes.stream().filter(c -> c.code() == 200).findFirst().orElseThrow();

        assertEquals(2, code200.totalResponsesCount());
    }

    private LogEntry createLogEntry(String resource, int status, long size) {
        return createLogEntry(resource, "HTTP/1.1", status, size);
    }

    private LogEntry createLogEntry(String resource, String protocol, int status, long size) {
        return new LogEntry(
                "127.0.0.1", "-", LocalDateTime.now(), "GET", resource, protocol, status, size, "-", "Mozilla/5.0");
    }
}
