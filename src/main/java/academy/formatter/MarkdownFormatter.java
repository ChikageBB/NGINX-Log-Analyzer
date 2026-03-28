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
public class MarkdownFormatter implements OutputFormatter {

    @Override
    public void write(Statistics stats, String outputPath) {
        try {
            log.info("Writing Markdown to: {}", outputPath);
            StringBuilder md = new StringBuilder();

            appendGeneralInfo(md, stats);
            appendResources(md, stats);
            appendResponseCodes(md, stats);

            if (!stats.requestsPerDate().isEmpty()) {
                appendRequestsPerDate(md, stats);
            }

            if (!stats.uniqueProtocols().isEmpty()) {
                appendProtocols(md, stats);
            }

            Files.writeString(Path.of(outputPath), md.toString());
            log.info("Markdown written successfully");
        } catch (IOException e) {
            log.error("Failed to write Markdown", e);
            throw new RuntimeException("Failed to write Markdown output to " + outputPath, e);
        }
    }

    private void appendGeneralInfo(StringBuilder md, Statistics stats) {
        md.append("#### Общая информация\n\n");
        md.append("|        Метрика        |     Значение |\n");
        md.append("|:---------------------:|-------------:|\n");
        md.append(String.format("|       Файл(-ы)        | `%s` |\n", String.join(", ", stats.files())));
        md.append(String.format("|  Количество запросов  |       %,d |\n", stats.totalRequestsCount()));
        md.append(String.format(
                "| Средний размер ответа |         %.2f |\n",
                stats.responseSizeInBytes().average()));
        md.append(String.format(
                "|  95p размера ответа   |         %.2f |\n",
                stats.responseSizeInBytes().p95()));
        md.append("\n");
    }

    private void appendResources(StringBuilder md, Statistics stats) {
        md.append("#### Запрашиваемые ресурсы\n\n");
        md.append("|     Ресурс      | Количество |\n");
        md.append("|:---------------:|-----------:|\n");

        for (ResourceStat resource : stats.resources()) {
            md.append(String.format("|  `%s`  |      %,d |\n", resource.resource(), resource.totalRequestsCount()));
        }
        md.append("\n");
    }

    private void appendResponseCodes(StringBuilder md, Statistics stats) {
        md.append("#### Коды ответа\n\n");
        md.append("| Код |          Имя          | Количество |\n");
        md.append("|:---:|:---------------------:|-----------:|\n");

        for (ResponseCodeStats code : stats.responseCodes()) {
            md.append(String.format(
                    "| %d | %s | %,d |\n",
                    code.code(), HttpStatus.getReasonPhraseByCode(code.code()), code.totalResponsesCount()));
        }
        md.append("\n");
    }

    private void appendRequestsPerDate(StringBuilder md, Statistics stats) {
        md.append("#### Распределение по датам\n\n");
        md.append("| Дата | День недели | Количество | Процент |\n");
        md.append("|:----:|:-----------:|-----------:|--------:|\n");

        for (DateStat date : stats.requestsPerDate()) {
            md.append(String.format(
                    "| %s | %s | %,d | %.2f%% |\n",
                    date.date(), date.weekday(), date.totalRequestsCount(), date.totalRequestsPercentage()));
        }
        md.append("\n");
    }

    private void appendProtocols(StringBuilder md, Statistics stats) {
        md.append("#### Уникальные протоколы\n\n");
        for (String protocol : stats.uniqueProtocols()) {
            md.append(String.format("- %s\n", protocol));
        }
        md.append("\n");
    }
}
