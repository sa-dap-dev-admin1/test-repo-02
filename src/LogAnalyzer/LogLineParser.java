package loganalyzer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogLineParser {

    private static final Pattern BASE_LOG_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} GMT)\\s+\\S+\\s+(.*?)\\s+\\S+\\s+-\\s+(.*)$"
    );
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS 'GMT'");

    private static final String MSG_START = "messagePattern=Starting AutoFix data generation";
    private static final String MSG_ERROR = "messagePattern=Error in getting Single file autofix generation";
    private static final String MSG_END_WITH_ERROR = "How-To-Fix payload created";
    private static final String MSG_END_SUCCESS = "messagePattern=Autofix process completed successfully";

    private final Map<String, RequestData> allRequests;
    private final Map<String, String> threadToRequestMap;
    private final LinkedList<String> pendingQueue;

    public LogLineParser(Map<String, RequestData> allRequests,
                         Map<String, String> threadToRequestMap,
                         LinkedList<String> pendingQueue) {
        this.allRequests = allRequests;
        this.threadToRequestMap = threadToRequestMap;
        this.pendingQueue = pendingQueue;
    }

    public void parseLine(String line) {
        Matcher baseMatcher = BASE_LOG_PATTERN.matcher(line);
        if (!baseMatcher.find()) return;

        String timestampStr = baseMatcher.group(1);
        String threadName = baseMatcher.group(2).trim();
        String message = baseMatcher.group(3);

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(timestampStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return;
        }

        String argsContent = extractArgsContent(message);

        if (message.contains(MSG_START)) {
            handleStart(message, threadName, timestamp, argsContent);
        } else if (message.contains(MSG_ERROR)) {
            handleError(message, timestamp, argsContent);
        } else if (message.contains(MSG_END_WITH_ERROR)) {
            handleEndWithError(message, timestamp);
        } else if (message.contains(MSG_END_SUCCESS)) {
            handleEndSuccess(threadName, timestamp);
        }
    }

    private String extractArgsContent(String message) {
        int startIdx = message.indexOf("stringArgs=[");
        if (startIdx == -1) return null;

        startIdx += "stringArgs=[".length();
        int endIdx = message.lastIndexOf(", throwable=");
        if (endIdx == -1) endIdx = message.lastIndexOf("]");
        if (endIdx > startIdx) return message.substring(startIdx, endIdx);
        return null;
    }

    private void handleStart(String message, String threadName, LocalDateTime timestamp, String argsContent) {
        if (argsContent == null) return;

        String requestId = argsContent.trim();
        if (requestId.endsWith("]")) requestId = requestId.substring(0, requestId.length() - 1);
        if (requestId.startsWith("[")) requestId = requestId.substring(1);
        if (requestId.contains(",")) requestId = requestId.split(",")[0].trim();
        requestId = requestId.trim();

        RequestData data = allRequests.getOrDefault(requestId, new RequestData());
        data.requestId = requestId;
        data.startTime = timestamp;

        allRequests.put(requestId, data);
        threadToRequestMap.put(threadName, requestId);
        pendingQueue.add(requestId);
    }

    private void handleError(String message, LocalDateTime timestamp, String argsContent) {
        if (argsContent == null) return;

        String args = argsContent;
        if (args.endsWith("]")) args = args.substring(0, args.length() - 1);

        String requestId = null;
        String errorMsg = null;

        int lastComma = args.lastIndexOf(",");
        if (lastComma != -1) {
            requestId = args.substring(lastComma + 1).trim();
            errorMsg = args.substring(0, lastComma).trim();
        }

        if (requestId != null) {
            RequestData data = allRequests.getOrDefault(requestId, new RequestData());
            data.requestId = requestId;
            data.errorReason = errorMsg;
            data.endTime = timestamp;
            data.confidence = "HIGH";

            allRequests.put(requestId, data);
            pendingQueue.remove(requestId);
            threadToRequestMap.values().remove(requestId);
        }
    }

    private void handleEndWithError(String message, LocalDateTime timestamp) {
        Matcher m = Pattern.compile("RequestID: ([\\w-]+)\\.").matcher(message);
        if (m.find()) {
            String requestId = m.group(1);
            RequestData data = allRequests.get(requestId);
            if (data != null) {
                data.endTime = timestamp;
                data.confidence = "HIGH";
                pendingQueue.remove(requestId);
                threadToRequestMap.values().remove(requestId);
            }
        }
    }

    private void handleEndSuccess(String threadName, LocalDateTime timestamp) {
        String requestId = threadToRequestMap.get(threadName);

        if (requestId != null) {
            RequestData data = allRequests.get(requestId);
            if (data != null) {
                data.endTime = timestamp;
                data.completedNormally = true;
                data.confidence = "HIGH";
                pendingQueue.remove(requestId);
                threadToRequestMap.remove(threadName);
            }
            return;
        }

        if (!pendingQueue.isEmpty()) {
            String fallbackId = pendingQueue.poll();
            RequestData data = allRequests.get(fallbackId);
            if (data != null) {
                data.endTime = timestamp;
                data.completedNormally = true;
                data.confidence = "LOW";
                threadToRequestMap.values().remove(fallbackId);
            }
        }
    }
}
