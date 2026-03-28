package academy.formatter;

import academy.model.DateStat;
import academy.model.HttpStatus;
import academy.model.ResourceStat;
import academy.model.ResponseCodeStats;
import academy.model.Statistics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdocFormatter implements OutputFormatter {

    @Override
    public void write(Statistics statistics, String output) {
        try {
            log.info("Writing AsciiDoc to: {}", output);

            StringBuilder adoc = new StringBuilder();

            adoc.append("= NGINX Log Analysis Report\n");
            adoc.append(":toc:\n");
            adoc.append(":toc-title: Содержание\n\n");

            appendGeneralInfo(adoc, statistics);
            appendResources(adoc, statistics);
            appendResponseCodes(adoc, statistics);

            if (!statistics.requestsPerDate().isEmpty()) {
                appendRequestsPerDate(adoc, statistics);
            }

            if (!statistics.uniqueProtocols().isEmpty()) {
                appendProtocols(adoc, statistics);
            }

            Files.writeString(Path.of(output), adoc.toString());
            log.info("AsciiDoc written successfully to: {}", output);
        } catch (IOException e) {
            log.error("Failed to write AsciiDoc to: {}", output, e);
            throw new RuntimeException("Failed to write AsciiDoc output to " + output, e);
        }
    }

    private void appendGeneralInfo(StringBuilder adoc, Statistics statistics) {
        adoc.append("== Общая информация\n\n");
        adoc.append("[cols=\"1,1\"]\n");
        adoc.append("|===\n");
        adoc.append("| Метрика | Значение\n\n");
        adoc.append(String.format("| Файл(-ы) | `%s`\n", String.join(", ", statistics.files())));
        adoc.append(String.format("| Количество запросов | %,d\n", statistics.totalRequestsCount()));
        adoc.append(String.format(
                "| Средний размер ответа | %.2f байт\n",
                statistics.responseSizeInBytes().average()));
        adoc.append(String.format(
                "| Максимальный размер | %.2f байт\n",
                statistics.responseSizeInBytes().max()));
        adoc.append(String.format(
                "| 95-й перцентиль | %.2f байт\n",
                statistics.responseSizeInBytes().p95()));
        adoc.append("|===\n\n");
    }

    private void appendResources(StringBuilder adoc, Statistics statistics) {
        adoc.append("== Запрашиваемые ресурсы\n\n");
        adoc.append("[cols=\"2,1\"]\n");
        adoc.append("|===\n");
        adoc.append("| Ресурс | Количество запросов\n\n");
        for (ResourceStat resource : statistics.resources()) {
            adoc.append(String.format("| `%s` | %,d\n", resource.resource(), resource.totalRequestsCount()));
        }
        adoc.append("|===\n\n");
    }

    private void appendResponseCodes(StringBuilder adoc, Statistics statistics) {
        adoc.append("== Коды ответа\n\n");
        adoc.append("[cols=\"1,2,1\"]\n");
        adoc.append("|===\n");
        adoc.append("| Код | Имя | Количество\n\n");
        for (ResponseCodeStats code : statistics.responseCodes()) {
            adoc.append(String.format(
                    "| %d | %s | %,d\n",
                    code.code(), HttpStatus.getReasonPhraseByCode(code.code()), code.totalResponsesCount()));
        }
        adoc.append("|===\n\n");
    }

    private void appendRequestsPerDate(StringBuilder adoc, Statistics statistics) {
        adoc.append("== Распределение запросов по датам\n\n");
        adoc.append("[cols=\"1,2,1,1\"]\n");
        adoc.append("|===\n");
        adoc.append("| Дата | День недели | Количество | Процент\n\n");
        for (DateStat date : statistics.requestsPerDate()) {
            adoc.append(String.format(
                    "| %s | %s | %,d | %.2f%%\n",
                    date.date(), date.weekday(), date.totalRequestsCount(), date.totalRequestsPercentage()));
        }
        adoc.append("|===\n\n");
    }

    private void appendProtocols(StringBuilder adoc, Statistics statistics) {
        adoc.append("== Уникальные протоколы\n\n");
        for (String protocol : statistics.uniqueProtocols()) {
            adoc.append(String.format("* %s\n", protocol));
        }
        adoc.append("\n");
    }
}
