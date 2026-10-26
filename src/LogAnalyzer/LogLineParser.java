package LogAnalyzer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LogLineParser {

    private static final Pattern BASE_LOG_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} GMT)\\s+\\S+\\s+(.*?)\\s+\\S+\\s+-\\s+(.*)$"
    );
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS 'GMT'");

    private static final String MSG_START = "messagePattern=Starting AutoFix data generation";
    private static final String MSG_ERROR = "messagePattern=Error in getting Single file autofix generation";
    private static final String MSG_END_WITH_ERROR = "How-To-Fix payload created";
    private static final String MSG_END_SUCCESS = "messagePattern=Autofix process completed successfully";

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("RequestID: ([\\w-]+)\\.");

    static void parseLine(String line, Map<String, LogAnalyzer.RequestData> allRequests,
                          Map<String, String> threadToRequestMap, LinkedList<String> pendingQueue) {
        Matcher baseMatcher = BASE_LOG_PATTERN.matcher(line);
        if (!baseMatcher.find()) return;

        String timestampStr = baseMatcher.group(1);
        String threadName = baseMatcher.group(2).trim();
        String message = baseMatcher.group(3);

        LocalDateTime timestamp = parseTimestamp(timestampStr);
        if (timestamp == null) return;

        String argsContent = extractArgsContent(message);

        if (message.contains(MSG_START) && argsContent != null) {
            handleStartMessage(argsContent, threadName, timestamp, allRequests, threadToRequestMap, pendingQueue);
        } else if (message.contains(MSG_ERROR) && argsContent != null) {
            handleErrorMessage(argsContent, timestamp, allRequests, threadToRequestMap, pendingQueue);
        } else if (message.contains(MSG_END_WITH_ERROR)) {
            handleEndWithErrorMessage(message, timestamp, allRequests, threadToRequestMap, pendingQueue);
        } else if (message.contains(MSG_END_SUCCESS)) {
            handleEndSuccessMessage(threadName, timestamp, allRequests, threadToRequestMap, pendingQueue);
        }
    }

    private static LocalDateTime parseTimestamp(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
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

    private static String cleanRequestId(String raw) {
        String id = raw.trim();
        if (id.endsWith("]")) id = id.substring(0, id.length() - 1);
        if (id.startsWith("[")) id = id.substring(1);
        if (id.contains(",")) id = id.split(",")[0].trim();
        return id.trim();
    }

    private static void handleStartMessage(String argsContent, String threadName, LocalDateTime timestamp,
                                           Map<String, LogAnalyzer.RequestData> allRequests,
                                           Map<String, String> threadToRequestMap,
                                           LinkedList<String> pendingQueue) {
        String requestId = cleanRequestId(argsContent);
        LogAnalyzer.RequestData data = allRequests.getOrDefault(requestId, new LogAnalyzer.RequestData());
        data.requestId = requestId;
        data.startTime = timestamp;
        allRequests.put(requestId, data);
        threadToRequestMap.put(threadName, requestId);
        pendingQueue.add(requestId);
    }

    private static void handleErrorMessage(String argsContent, LocalDateTime timestamp,
                                           Map<String, LogAnalyzer.RequestData> allRequests,
                                           Map<String, String> threadToRequestMap,
                                           LinkedList<String> pendingQueue) {
        String args = argsContent.endsWith("]") ? argsContent.substring(0, argsContent.length() - 1) : argsContent;
        int lastComma = args.lastIndexOf(",");
        if (lastComma == -1) return;

        String requestId = args.substring(lastComma + 1).trim();
        String errorMsg = args.substring(0, lastComma).trim();

        LogAnalyzer.RequestData data = allRequests.getOrDefault(requestId, new LogAnalyzer.RequestData());
        data.requestId = requestId;
        data.errorReason = errorMsg;
        data.endTime = timestamp;
        data.confidence = "HIGH";
        allRequests.put(requestId, data);
        pendingQueue.remove(requestId);
        threadToRequestMap.values().remove(requestId);
    }

    private static void handleEndWithErrorMessage(String message, LocalDateTime timestamp,
                                                  Map<String, LogAnalyzer.RequestData> allRequests,
                                                  Map<String, String> threadToRequestMap,
                                                  LinkedList<String> pendingQueue) {
        Matcher m = REQUEST_ID_PATTERN.matcher(message);
        if (!m.find()) return;
        String requestId = m.group(1);
        LogAnalyzer.RequestData data = allRequests.get(requestId);
        if (data == null) return;
        data.endTime = timestamp;
        data.confidence = "HIGH";
        pendingQueue.remove(requestId);
        threadToRequestMap.values().remove(requestId);
    }

    private static void handleEndSuccessMessage(String threadName, LocalDateTime timestamp,
                                                Map<String, LogAnalyzer.RequestData> allRequests,
                                                Map<String, String> threadToRequestMap,
                                                LinkedList<String> pendingQueue) {
        String requestId = threadToRequestMap.get(threadName);
        if (requestId != null) {
            applySuccessToRequest(allRequests.get(requestId), timestamp, "HIGH");
            pendingQueue.remove(requestId);
            threadToRequestMap.remove(threadName);
            return;
        }
        if (pendingQueue.isEmpty()) return;
        String fallbackId = pendingQueue.poll();
        applySuccessToRequest(allRequests.get(fallbackId), timestamp, "LOW");
        threadToRequestMap.values().remove(fallbackId);
    }

    private static void applySuccessToRequest(LogAnalyzer.RequestData data, LocalDateTime timestamp,
                                              String confidence) {
        if (data == null) return;
        data.endTime = timestamp;
        data.completedNormally = true;
        data.confidence = confidence;
    }
}
