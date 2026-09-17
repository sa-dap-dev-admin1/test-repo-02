package LogAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void processFile(Path file, Map<String, RequestData> allRequests,
                                   Map<String, String> threadToRequestMap, LinkedList<String> pendingQueue) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, allRequests, threadToRequestMap, pendingQueue);
            }
        } catch (IOException e) {
            System.err.println("   [ERROR] Failed to read file: " + e.getMessage());
        }
    }

    private static void parseLine(String line, Map<String, RequestData> allRequests,
                                  Map<String, String> threadToRequestMap, LinkedList<String> pendingQueue) {
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

        if (message.contains(MSG_START) && argsContent != null) {
            handleStart(argsContent, threadName, timestamp, allRequests, threadToRequestMap, pendingQueue);
        } else if (message.contains(MSG_ERROR) && argsContent != null) {
            handleError(argsContent, timestamp, allRequests, threadToRequestMap, pendingQueue);
        } else if (message.contains(MSG_END_WITH_ERROR)) {
            handleEndWithError(message, timestamp, allRequests, threadToRequestMap, pendingQueue);
        } else if (message.contains(MSG_END_SUCCESS)) {
            handleEndSuccess(threadName, timestamp, allRequests, threadToRequestMap, pendingQueue);
        }
    }

    private static String extractArgsContent(String message) {
        int startIdx = message.indexOf("stringArgs=[");
        if (startIdx == -1) return null;
        startIdx += "stringArgs=[".length();
        int endIdx = message.lastIndexOf(", throwable=");
        if (endIdx == -1) endIdx = message.lastIndexOf("]");
        if (endIdx > startIdx) return message.substring(startIdx, endIdx);
        return null;
    }

    /**
     * Handles the START state-machine event.
     * Creates a new RequestData and maps the current thread to the request ID.
     */
    private static void handleStart(String argsContent, String threadName, LocalDateTime timestamp,
                                    Map<String, RequestData> allRequests,
                                    Map<String, String> threadToRequestMap,
                                    LinkedList<String> pendingQueue) {
        String requestId = argsContent.trim();
        if (requestId.endsWith("]")) requestId = requestId.substring(0, requestId.length() - 1);
        if (requestId.startsWith("[")) requestId = requestId.substring(1);
        if (requestId.contains(",")) requestId = requestId.split(",")[0].trim();
        requestId = requestId.trim();

        RequestData data = allRequests.getOrDefault(requestId, new RequestData());
        data.setRequestId(requestId);
        data.setStartTime(timestamp);

        allRequests.put(requestId, data);
        threadToRequestMap.put(threadName, requestId);
        pendingQueue.add(requestId);
    }

    /**
     * Handles the ERROR state-machine event.
     * Sets the error reason and end time on the matching RequestData.
     */
    private static void handleError(String argsContent, LocalDateTime timestamp,
                                    Map<String, RequestData> allRequests,
                                    Map<String, String> threadToRequestMap,
                                    LinkedList<String> pendingQueue) {
        if (argsContent.endsWith("]")) argsContent = argsContent.substring(0, argsContent.length() - 1);

        String requestId = null;
        String errorMsg = null;

        int lastComma = argsContent.lastIndexOf(",");
        if (lastComma != -1) {
            requestId = argsContent.substring(lastComma + 1).trim();
            errorMsg = argsContent.substring(0, lastComma).trim();
        }

        if (requestId != null) {
            RequestData data = allRequests.getOrDefault(requestId, new RequestData());
            data.setRequestId(requestId);
            data.setErrorReason(errorMsg);
            data.setEndTime(timestamp);
            data.setConfidence("HIGH");

            allRequests.put(requestId, data);
            pendingQueue.remove(requestId);
            threadToRequestMap.values().remove(requestId);
        }
    }

    /**
     * Handles the END_WITH_ERROR state-machine event.
     * Extracts the request ID from the message via regex and marks the request as ended.
     */
    private static void handleEndWithError(String message, LocalDateTime timestamp,
                                           Map<String, RequestData> allRequests,
                                           Map<String, String> threadToRequestMap,
                                           LinkedList<String> pendingQueue) {
        Matcher m = Pattern.compile("RequestID: ([\\w-]+)\\.").matcher(message);
        if (m.find()) {
            String requestId = m.group(1);
            RequestData data = allRequests.get(requestId);
            if (data != null) {
                data.setEndTime(timestamp);
                data.setConfidence("HIGH");
                pendingQueue.remove(requestId);
                threadToRequestMap.values().remove(requestId);
            }
        }
    }

    /**
     * Handles the END_SUCCESS state-machine event.
     * Uses the thread map for HIGH confidence correlation, falls back to FIFO queue with LOW confidence.
     */
    private static void handleEndSuccess(String threadName, LocalDateTime timestamp,
                                         Map<String, RequestData> allRequests,
                                         Map<String, String> threadToRequestMap,
                                         LinkedList<String> pendingQueue) {
        String requestId = threadToRequestMap.get(threadName);

        if (requestId != null) {
            RequestData data = allRequests.get(requestId);
            if (data != null) {
                data.setEndTime(timestamp);
                data.setCompletedNormally(true);
                data.setConfidence("HIGH");
                pendingQueue.remove(requestId);
                threadToRequestMap.remove(threadName);
            }
            return;
        }

        if (!pendingQueue.isEmpty()) {
            String fallbackId = pendingQueue.poll();
            RequestData data = allRequests.get(fallbackId);
            if (data != null) {
                data.setEndTime(timestamp);
                data.setCompletedNormally(true);
                data.setConfidence("LOW");
                threadToRequestMap.values().remove(fallbackId);
            }
        }
    }
}
