package academy.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLogReader implements LogReader {

    private final String url;

    public RemoteLogReader(final String url) {
        this.url = url;
    }

    @Override
    public Stream<String> readLines() {
        try {

            HttpURLConnection connection =
                    (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new IOException("Remote Log Not Found (404): " + url);
            }

            if (!(responseCode == HttpURLConnection.HTTP_OK)) {
                throw new RuntimeException("Failed to fetch remote file. HTTP " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            return reader.lines().onClose(() -> {
                try {
                    reader.close();
                    connection.disconnect();
                } catch (IOException e) {
                    log.error("Failed to fetch remote file", e);
                }
            });

        } catch (Exception e) {
            log.error("Failed to read remote file {}", url, e);
            throw new RuntimeException("Failed to connect to remote log reader", e);
        }
    }
}
