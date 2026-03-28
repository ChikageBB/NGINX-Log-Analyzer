package academy.formatter;

import academy.model.Statistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonFormatter implements OutputFormatter {
    private final ObjectMapper mapper;

    public JsonFormatter() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public void write(Statistics statistics, String output) {
        try {
            log.info("Writing JSON to: {}", output);
            String json = mapper.writeValueAsString(statistics);
            Files.write(Path.of(output), json.getBytes());
            log.info("JSON written to: {}", output);
        } catch (IOException e) {
            log.error("Failed to write JSON to: {}", output, e);
            throw new RuntimeException(String.format("Failed to save statistics to file %s", output), e);
        }
    }
}
