package academy.statistics;

import academy.model.DateStat;
import academy.model.LogEntry;
import academy.model.ResourceStat;
import academy.model.ResponseCodeStats;
import academy.model.ResponseSizeStats;
import academy.model.Statistics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsCalculator {

    public Statistics calculateStatistics(Stream<LogEntry> entries, List<String> files) {
        List<LogEntry> entryList = entries.toList();

        return Statistics.builder()
                .files(files)
                .totalRequestsCount(calculateTotalRequest(entryList))
                .responseSizeInBytes(calculateResponseSizeStats(entryList))
                .resources(calculateTopResources(entryList))
                .responseCodes(calculateResponseCodes(entryList))
                .requestsPerDate(calculateRequestsPerDate(entryList))
                .uniqueProtocols(calculateUniqueProtocols(entryList))
                .build();
    }

    private long calculateTotalRequest(List<LogEntry> entries) {
        return entries.size();
    }

    private ResponseSizeStats calculateResponseSizeStats(List<LogEntry> entries) {
        var sizes = entries.stream().map(LogEntry::bodyBytesSent).sorted().toList();

        if (sizes.isEmpty()) {
            return new ResponseSizeStats(0, 0, 0);
        }

        double average = calculateAverage(sizes);
        double max = calculateMax(sizes);
        double p95 = calculate95Percentile(sizes);

        return new ResponseSizeStats(average, max, p95);
    }

    private double calculateMax(List<Long> sortedValues) {
        return sortedValues.getLast();
    }

    private double calculateAverage(List<Long> sortedValues) {
        double average =
                sortedValues.stream().mapToLong(Long::longValue).average().orElse(0);
        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double calculate95Percentile(List<Long> sortedValues) {
        if (sortedValues.isEmpty()) {
            return 0.0;
        }
        double rank = 0.95 * (sortedValues.size() - 1);
        int low = (int) Math.floor(rank);
        int high = (int) Math.ceil(rank);

        if (low == high) {
            return sortedValues.get(low);
        }

        double fraction = rank - low;

        return sortedValues.get(low) * (1 - fraction) + sortedValues.get(high) * fraction;
    }

    private List<ResourceStat> calculateTopResources(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(LogEntry::resource, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new ResourceStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ResponseCodeStats> calculateResponseCodes(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(LogEntry::status, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .map(e -> new ResponseCodeStats(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<DateStat> calculateRequestsPerDate(List<LogEntry> entries) {
        long total = entries.size();

        return entries.stream()
                .collect(Collectors.groupingBy(e -> e.timestamp().toLocalDate(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    double percentage = e.getValue() * 100.0 / total;
                    percentage = BigDecimal.valueOf(percentage)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    return new DateStat(
                            e.getKey().toString(),
                            e.getKey().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                            e.getValue(),
                            percentage);
                })
                .toList();
    }

    private List<String> calculateUniqueProtocols(List<LogEntry> entries) {
        return entries.stream().map(LogEntry::protocol).distinct().toList();
    }
}
