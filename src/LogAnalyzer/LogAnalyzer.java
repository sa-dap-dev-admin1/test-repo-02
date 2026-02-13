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
    private static final String MSG_START = "messagePattern=Starting AutoFix data generation";
    private static final String MSG_ERROR = "messagePattern=Error in getting Single file autofix generation";
    private static final String MSG_END_WITH_ERROR = "How-To-Fix payload created"; // Explicit ID -> High Confidence
    private static final String MSG_END_SUCCESS = "messagePattern=Autofix process completed successfully"; // No ID -> Thread/FIFO Check

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, "Desktop", "integrator-server-logs");

        if (args.length > 0) logPath = Paths.get(args[0]);

        System.out.println("==================================================");
        System.out.println("LOG ANALYZER v6.0 (Confidence Levels)");
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

        // SORT FILES (Month Rollover)
        Collections.sort(logFiles, new Comparator<Path>() {
            @Override
            public int compare(Path p1, Path p2) {
                try {
                    String n1 = p1.getFileName().toString();
                    String n2 = p2.getFileName().toString();

                    if (n1.equals("integrator-server.log")) return 1;
                    if (n2.equals("integrator-server.log")) return -1;

                    String s1 = n1.substring(n1.lastIndexOf(".") + 1);
                    String s2 = n2.substring(n2.lastIndexOf(".") + 1);
                    String[] parts1 = s1.split("-");
                    String[] parts2 = s2.split("-");

                    int day1 = Integer.parseInt(parts1[0]);
                    int day2 = Integer.parseInt(parts2[0]);

                    if (day1 > 20 && day2 < 10) return -1;
                    if (day1 < 10 && day2 > 20) return 1;
                    if (day1 != day2) return Integer.compare(day1, day2);

                    return Integer.compare(Integer.parseInt(parts1[1]), Integer.parseInt(parts2[1]));
                } catch (Exception e) {
                    return p1.compareTo(p2);
                }
            }
        });

        System.out.println("[INFO] Processing " + logFiles.size() + " files.");

        Map<String, RequestData> allRequests = new HashMap<>();
        Map<String, String> threadToRequestMap = new HashMap<>();
        LinkedList<String> pendingQueue = new LinkedList<>(); // For FIFO fallback

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

        // --- EXTRACT ARGS ---
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
            if (requestId.contains(",")) requestId = requestId.split(",")[0].trim();

            RequestData data = allRequests.getOrDefault(requestId, new RequestData());
            data.requestId = requestId;
            data.startTime = timestamp;

            allRequests.put(requestId, data);
            threadToRequestMap.put(threadName, requestId); // Bind thread
            pendingQueue.add(requestId); // Add to FIFO queue
            return;
        }

        // 2. L2: ERROR (Explicit ID -> HIGH Confidence)
        if (message.contains(MSG_ERROR) && argsContent != null) {
            // Args: [Error Msg, RequestID] -> ID is usually last
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
                data.endTime = timestamp; // Approximate end time
                data.confidence = "HIGH"; // Explicit ID match

                allRequests.put(requestId, data);
                pendingQueue.remove(requestId); // Done
                threadToRequestMap.values().remove(requestId); // Unbind
            }
            return;
        }

        // 3. L3 Case A: END WITH ERROR (Explicit ID -> HIGH Confidence)
        if (message.contains(MSG_END_WITH_ERROR)) {
            // Regex for: RequestID: scan_xxx.
            Matcher m = Pattern.compile("RequestID: ([\\w-]+)\\.").matcher(message);
            if (m.find()) {
                String requestId = m.group(1);
                RequestData data = allRequests.get(requestId);
                if (data != null) {
                    data.endTime = timestamp;
                    data.confidence = "HIGH"; // Explicit ID match

                    pendingQueue.remove(requestId);
                    threadToRequestMap.values().remove(requestId);
                }
            }
            return;
        }

        // 4. L3 Case B: END SUCCESS (No ID -> Strategy Check)
        if (message.contains(MSG_END_SUCCESS)) {
            // STRATEGY 1: THREAD MATCH (High Confidence)
            String requestId = threadToRequestMap.get(threadName);

            if (requestId != null) {
                RequestData data = allRequests.get(requestId);
                if (data != null) {
                    data.endTime = timestamp;
                    data.completedNormally = true;
                    data.confidence = "HIGH"; // Matched by specific thread

                    pendingQueue.remove(requestId);
                    threadToRequestMap.remove(threadName);
                }
                return; // Found match, exit
            }

            // STRATEGY 2: FIFO FALLBACK (Low Confidence)
            // If we are here, thread match failed. Use oldest pending request.
            if (!pendingQueue.isEmpty()) {
                String fallbackId = pendingQueue.poll();

                RequestData data = allRequests.get(fallbackId);
                if (data != null) {
                    data.endTime = timestamp;
                    data.completedNormally = true;
                    data.confidence = "LOW"; // Fallback guess

                    // Try to clean up thread map if possible
                    threadToRequestMap.values().remove(fallbackId);
                }
            }
        }
    }

    private static void printFinalReport(Map<String, RequestData> requestMap) {
        System.out.println("\n");
        System.out.println("=======================================================================================================================");
        System.out.println("FINAL ANALYSIS REPORT");
        System.out.println("Total Requests Found: " + requestMap.size());
        System.out.println("=======================================================================================================================");

        if (requestMap.isEmpty()) {
            System.out.println("No requests found.");
            return;
        }

        System.out.printf("%-36s | %-12s | %-12s | %-10s | %s%n", "Request ID", "Duration(ms)", "Status", "Confidence", "Error Reason");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        List<RequestData> sortedRequests = new ArrayList<>(requestMap.values());
        sortedRequests.sort((r1, r2) -> {
            if (r1.startTime == null) return 1;
            if (r2.startTime == null) return -1;
            return r1.startTime.compareTo(r2.startTime);
        });

        for (RequestData req : sortedRequests) {
            long duration = 0;
            String status = "UNKNOWN";

            if (req.errorReason != null) status = "FAILED";
            else if (req.completedNormally) status = "SUCCESS";
            else status = "INCOMPLETE";

            if (req.startTime != null && req.endTime != null) {
                duration = ChronoUnit.MILLIS.between(req.startTime, req.endTime);
            }

            String conf = (req.confidence != null) ? req.confidence : "-";

            String cleanError = (req.errorReason == null) ? "-" : req.errorReason;
            if (cleanError.startsWith("[") && cleanError.endsWith("]")) cleanError = cleanError.substring(1, cleanError.length()-1);
            if (cleanError.length() > 50) cleanError = cleanError.substring(0, 47) + "...";

            System.out.printf("%-36s | %-12d | %-12s | %-10s | %s%n",
                req.requestId, duration, status, conf, cleanError);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    static class RequestData {
        String requestId;
        LocalDateTime startTime;
        LocalDateTime endTime;
        String errorReason;
        boolean completedNormally = false;
        String confidence; // NEW FIELD
    }
}