package loganalyzer;

import java.time.LocalDateTime;

public class RequestData {
    public String requestId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String errorReason;
    public boolean completedNormally = false;
    public String confidence;
}
