package academy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class ApplicationTest {

    @Test
    @DisplayName("Базовая проверка работоспособности программы")
    void happyPathTest(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("log.txt");
        Files.write(
                logFile,
                List.of(
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3\"",
                        "93.180.71.3 - - [17/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 1024 \"-\" \"Mozilla/5.0\""));

        Path outputFile = tempDir.resolve("output.md");

        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        int exitCode = cmd.execute(
                "--path", logFile.toString(),
                "--output", outputFile.toString(),
                "--format", "markdown");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("Общая информация"));
        assertTrue(content.contains("2")); // 2 запроса
    }
}
