package academy.model;

import java.util.List;
import lombok.Builder;

@Builder
public record Statistics(
        List<String> files,
        long totalRequestsCount,
        ResponseSizeStats responseSizeInBytes,
        List<ResourceStat> resources,
        List<ResponseCodeStats> responseCodes,
        List<DateStat> requestsPerDate,
        List<String> uniqueProtocols) {}
