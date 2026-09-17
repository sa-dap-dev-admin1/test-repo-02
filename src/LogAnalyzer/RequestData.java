package LogAnalyzer;

import java.time.LocalDateTime;

public class RequestData {

    private String requestId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorReason = null;
    private boolean completedNormally = false;
    private String confidence = null;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public boolean isCompletedNormally() {
        return completedNormally;
    }

    public void setCompletedNormally(boolean completedNormally) {
        this.completedNormally = completedNormally;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
