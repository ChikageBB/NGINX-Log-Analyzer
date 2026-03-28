package academy.cli;

import java.time.LocalDate;
import java.util.List;

public class CliArguments {

    private List<String> paths;
    private String format = "markdown";
    private String output;
    private LocalDate fromDate;
    private LocalDate toDate;

    public CliArguments() {}

    public CliArguments(List<String> paths, String format, String output, LocalDate from, LocalDate toDate) {
        this.paths = paths;
        this.format = format;
        this.output = output;
        this.fromDate = from;
        this.toDate = toDate;
    }

    public List<String> getPaths() {
        return paths;
    }

    public String getFormat() {

        return format;
    }

    public String getOutput() {
        return output;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
