package LogAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    // --- CONFIGURATION ---
    private static final Pattern BASE_LOG_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} GMT)\\s+\\S+\\s+(.*?)\\s+\\S+\\s+-\\s+(.*)$"
    );
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS 'GMT'");

    // --- KEYWORDS ---
    private static final String MSG_START = "messagePasttern=Starting AutoFix data generation";
    private static final String MSG_ERROR = "messagePattern=Error in getting Single file autofix generation";
    private static final String MSG_END_WITH_ERROR = "How-To-Fix payload created";
    private static final String MSG_END_SUCCESS = "messagePattern=Autofix process completed successfully";

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, "Desktop", "integrator-server-logs");

        if (args.length > 0) logPath = Paths.get(args[0]);

        System.out.println("==================================================");
        System.out.println("LOG ANALYZER v11.0 (Summary First)");
        System.out.println("Scanning directory: " + logPath.toAbsolutePath());
        System.out.println("==================================================");

        if (!Files.exists(logPath)) {
            System.err.println("[FATAL] Folder not found at: " + logPath);
            return;
        }

        try {
            analyzeDirectory(logPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void analyzeDirectory(Path startPath) throws IOException {
        // 1. COLLECT FILES
        List<Path> logFiles = new ArrayList<>();
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().contains("integrator-server.log")) {
                    logFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // 2. SORT FILES (Pivot Logic)
        Pattern datePattern = Pattern.compile("\\.(\\d{2})-(\\d+)");

        Collections.sort(logFiles, new Comparator<Path>() {
            @Override
            public int compare(Path p1, Path p2) {
                String n1 = p1.getFileName().toString();
                String n2 = p2.getFileName().toString();

                if (n1.equals("integrator-server.log")) return 1;
                if (n2.equals("integrator-server.log")) return -1;

                int score1 = getChronologicalScore(n1);
                int score2 = getChronologicalScore(n2);

                if (score1 != score2) return Integer.compare(score1, score2);
                return n1.compareTo(n2);
            }

            private int getChronologicalScore(String filename) {
                Matcher m = datePattern.matcher(filename);
                if (m.find()) {
                    try {
                        int day = Integer.parseInt(m.group(1));
                        int rotation = Integer.parseInt(m.group(2));
                        int monthOffset = (day > 20) ? 0 : 100;
                        return (monthOffset + day) * 100 + rotation;
                    } catch (Exception e) { return 999999; }
                }
                return 999999;
            }
        });

        System.out.println("[INFO] Processing " + logFiles.size() + " files.");

        Map<String, RequestData> allRequests = new HashMap<>();
        Map<String, String> threadToRequestMap = new HashMap<>();
        LinkedList<String> pendingQueue = new LinkedList<>();

        for (Path file : logFiles) {
            System.out.println("   -> Reading: " + file.getFileName());
            processFile(file, allRequests, threadToRequestMap, pendingQueue);
        }

        printFinalReport(allRequests);
    }

    private static void processFile(Path file, Map<String, RequestData> allRequests,
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

        String argsContent = null;
        int startIdx = message.indexOf("stringArgs=[");
        if (startIdx != -1) {
            startIdx += "stringArgs=[".length();
            int endIdx = message.lastIndexOf(", throwable=");
            if (endIdx == -1) endIdx = message.lastIndexOf("]");
            if (endIdx > startIdx) argsContent = message.substring(startIdx, endIdx);
        }

        // 1. L1: START
        if (message.contains(MSG_START) && argsContent != null) {
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
            return;
        }

        // 2. L2: ERROR
        if (message.contains(MSG_ERROR) && argsContent != null) {
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
                data.requestId = requestId;
                data.errorReason = errorMsg;
                data.endTime = timestamp;
                data.confidence = "HIGH";

                allRequests.put(requestId, data);
                pendingQueue.remove(requestId);
                threadToRequestMap.values().remove(requestId);
            }
            return;
        }

        // 3. L3 Case A: END WITH ERROR
        if (message.contains(MSG_END_WITH_ERROR)) {
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
            return;
        }

        // 4. L3 Case B: END SUCCESS
        if (message.contains(MSG_END_SUCCESS)) {
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

    private static void printFinalReport(Map<String, RequestData> requestMap) {
        // 1. FILTER: VALID (Completed) requests only
        List<RequestData> validRequests = new ArrayList<>();
        int incompleteCount = 0;

        for (RequestData req : requestMap.values()) {
            boolean isFailed = (req.errorReason != null);
            boolean isSuccess = req.completedNormally;
            if (!isFailed && !isSuccess) {
                incompleteCount++;
            } else {
                validRequests.add(req);
            }
        }

        // 2. CALCULATE SUMMARY
        Map<String, Integer> summaryCounts = new HashMap<>();
        for (RequestData req : validRequests) {
            String cleanError = (req.errorReason == null) ? "-" : req.errorReason;
            if (cleanError.startsWith("[") && cleanError.endsWith("]")) {
                cleanError = cleanError.substring(1, cleanError.length()-1);
            }
            summaryCounts.put(cleanError, summaryCounts.getOrDefault(cleanError, 0) + 1);
        }

        // 3. PRINT HEADER & SUMMARY
        System.out.println("\n");
        System.out.println("=======================================================================================================================");
        System.out.println("FINAL ANALYSIS REPORT");
        System.out.println("Total Completed Requests: " + validRequests.size());
        System.out.println("(Excluded " + incompleteCount + " requests likely from CLI)");
        System.out.println("=======================================================================================================================");

        System.out.println("\n--- SUMMARY BY ERROR REASON ---");
        System.out.printf("%-80s | %s%n", "Error Reason", "Count");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        List<Map.Entry<String, Integer>> sortedSummary = new ArrayList<>(summaryCounts.entrySet());
        sortedSummary.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        for (Map.Entry<String, Integer> entry : sortedSummary) {
            String cleanKey = entry.getKey();
            if (cleanKey.length() > 77) cleanKey = cleanKey.substring(0, 74) + "...";
            System.out.printf("%-80s | %d%n", cleanKey, entry.getValue());
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");


        if (validRequests.isEmpty()) {
            System.out.println("\nNo completed requests found.");
            return;
        }

        // 4. SORT DETAILED ROWS
        validRequests.sort((r1, r2) -> {
            String e1 = (r1.errorReason == null) ? "ZZZZ_SUCCESS" : r1.errorReason;
            String e2 = (r2.errorReason == null) ? "ZZZZ_SUCCESS" : r2.errorReason;
            int errorCompare = e1.compareTo(e2);
            if (errorCompare != 0) return errorCompare;
            if (r1.startTime == null) return 1;
            if (r2.startTime == null) return -1;
            return r1.startTime.compareTo(r2.startTime);
        });

        // 5. PRINT DETAILED ROWS
        System.out.println("\n--- DETAILED REQUESTS ---");
        System.out.printf("%-36s | %-12s | %-12s | %-10s | %s%n", "Request ID", "Duration(s)", "Status", "Confidence", "Error Reason");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        for (RequestData req : validRequests) {
            double durationSeconds = 0.0;
            String status = "UNKNOWN";

            if (req.errorReason != null) status = "FAILED";
            else if (req.completedNormally) status = "SUCCESS";

            if (req.startTime != null && req.endTime != null) {
                long durationMillis = ChronoUnit.MILLIS.between(req.startTime, req.endTime);
                durationSeconds = durationMillis / 1000.0;
            }

            String conf = (req.confidence != null) ? req.confidence : "-";
            String cleanError = (req.errorReason == null) ? "-" : req.errorReason;

            if (cleanError.startsWith("[") && cleanError.endsWith("]")) cleanError = cleanError.substring(1, cleanError.length()-1);
            if (cleanError.length() > 50) cleanError = cleanError.substring(0, 47) + "...";

            System.out.printf("%-36s | %-12.2f | %-12s | %-10s | %s%n",
                req.requestId, durationSeconds, status, conf, cleanError);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    static class RequestData {
        String requestId;
        LocalDateTime startTime;
        LocalDateTime endTime;
        String errorReason;
        boolean completedNormally = false;
        String confidence;
    }
}
