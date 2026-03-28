package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.model.LogEntry;
import academy.parser.NginxParser;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NginxParserTest {

    private NginxParser nginxParser;

    @BeforeEach
    void setUp() {
        nginxParser = new NginxParser();
    }

    @Test
    @DisplayName("Парсинг валидной строки лога")
    void testParseValidLogLine() {
        String logLine =
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] " + "\"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" "
                        + "\"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Optional<LogEntry> result = nginxParser.parse(logLine);
        assertTrue(result.isPresent());

        LogEntry entry = nginxParser.parse(logLine).orElseThrow();
        assertEquals("93.180.71.3", entry.remoteAddress());
        assertEquals("-", entry.remoteUser());
        assertEquals("GET", entry.method());
        assertEquals("/downloads/product_1", entry.resource());
        assertEquals("HTTP/1.1", entry.protocol());
        assertEquals(304, entry.status());
        assertEquals(0, entry.bodyBytesSent());
        assertNotNull(entry.timestamp());
    }

    @Test
    @DisplayName("Парсинг невалидной строки возвращает empty")
    void testParseInvalidLogLine() {
        String logLine = "93.180.71.3 - -";

        Optional<LogEntry> result = nginxParser.parse(logLine);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Парсинг пустой строки")
    void testParseEmptyLogLine() {
        String logLine = "";
        Optional<LogEntry> result = nginxParser.parse(logLine);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Парсинг строки с некорректным статус-кодом")
    void testParseInvalidStatusCode() {
        String logLine =
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] " + "\"GET /downloads/product_1 HTTP/1.1\" ABC 0 \"-\" "
                        + "\"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        Optional<LogEntry> result = nginxParser.parse(logLine);
        assertTrue(result.isEmpty());
    }
}
