package academy.model;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record LogEntry(
        String remoteAddress,
        String remoteUser,
        LocalDateTime timestamp,
        String method,
        String resource,
        String protocol,
        int status,
        long bodyBytesSent,
        String httpReferer,
        String httpUserAgent) {}
