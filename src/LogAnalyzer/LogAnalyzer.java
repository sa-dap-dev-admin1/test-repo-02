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
import java.util.stream.Collectors;

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
        // 1. Collect all log files
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
        // Regex to find the pattern .DD-N (e.g., .27-1 or .01-1) anywhere in the name
        Pattern datePattern = Pattern.compile("\\.(\\d{2})-(\\d+)");

        Collections.sort(logFiles, new Comparator<Path>() {
            @Override
            public int compare(Path p1, Path p2) {
                String n1 = p1.getFileName().toString();
                String n2 = p2.getFileName().toString();

                // Active log always comes last
                boolean isMain1 = n1.equals("integrator-server.log");
                boolean isMain2 = n2.equals("integrator-server.log");
                if (isMain1 && !isMain2) return 1;
                if (!isMain1 && isMain2) return -1;
                if (isMain1 && isMain2) return 0;

                int score1 = getChronologicalScore(n1);
                int score2 = getChronologicalScore(n2);

                if (score1 != score2) {
                    return Integer.compare(score1, score2);
                }
                return n1.compareTo(n2); // Fallback to name if scores identical
            }

            // Helper to calculate a sortable score
            private int getChronologicalScore(String filename) {
                Matcher m = datePattern.matcher(filename);
                if (m.find()) {
                    try {
                        int day = Integer.parseInt(m.group(1));
                        int rotation = Integer.parseInt(m.group(2));

                        // PIVOT LOGIC:
                        // If Day > 20, we assume it's the Previous Month (Score = Day)
                        // If Day <= 20, we assume it's the Current Month (Score = Day + 100)
                        // Example: 27 -> 27.   01 -> 101.   13 -> 113.
                        // Result: 27 comes before 01 and 13.
                        int monthOffset = (day > 20) ? 0 : 100;

                        // Combine into a single number: MMMRR (MonthOffset + Rotation)
                        // We prioritize Month/Day first, then rotation index.
                        // Actually, day is enough for the main sort.
                        return (monthOffset + day) * 100 + rotation;
                    } catch (NumberFormatException e) {
                        return 999999; // Put parse errors at the end
                    }
                }
                return 999999; // No date found
            }
        });

        System.out.println("[INFO] Processing " + logFiles.size() + " files.");
        System.out.println("[INFO] First 5 files: " + logFiles.stream().limit(5).map(p -> p.getFileName().toString()).collect(Collectors.joining(", ")));
        System.out.println("[INFO] Last 5 files: " + logFiles.stream().skip(Math.max(0, logFiles.size() - 5)).map(p -> p.getFileName().toString()).collect(
            Collectors.joining(", ")));

        // 3. PROCESS
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