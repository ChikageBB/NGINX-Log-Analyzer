package academy.formatter;

import academy.model.Statistics;

public interface OutputFormatter {
    void write(Statistics statistics, String output);
}
