import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    // 1. LOG PATTERN CONFIGURATION
    // Standard Log4j format: "yyyy-MM-dd HH:mm:ss.SSS [Thread-Name] LEVEL Class - Message"
    private static final Pattern BASE_LOG_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+\\[(.*?)]\\s+(\\w+)\\s+.*?-\\s+(.*)$"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 2. SEARCH PATTERNS
    // L1: Start
    private static final Pattern START_PATTERN = Pattern.compile(
        "Starting AutoFix data generation for requestID: ([\\w-]+)"
    );

    // L2: Error (Combined logic)
    private static final Pattern ERROR_PATTERN = Pattern.compile(
        "(?:CLI Exception occurred|Unexpected error occurred).*?requestID: ([\\w-]+).*?Error: (.*)"
    );

    // L3: End
    private static final Pattern END_PATTERN = Pattern.compile(
        "Final response created with .*? payloads"
    );

    public static void main(String[] args) {
        // --- PATH CONFIGURATION ---
        // Automatically finds: C:\Users\YourName\Desktop\integrator-server-logs
        String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, "Desktop", "integrator-server-logs");

        // OPTIONAL: Override if you pass a path in "Program Arguments"
        if (args.length > 0) {
            logPath = Paths.get(args[0]);
        }

        System.out.println("Scanning directory: " + logPath.toAbsolutePath());

        if (!Files.exists(logPath)) {
            System.err.println("❌ ERROR: Folder not found!");
            System.err.println("   Expected at: " + logPath);
            System.err.println("   Please ensure a folder named 'integrator-server-logs' exists on your Desktop.");
            return;
        }

        // Print Header
        System.out.printf("%-36s | %-15s | %-10s | %s%n", "Request ID", "Duration (ms)", "Status", "Error Reason");
        System.out.println("--------------------------------------------------------------------------------------------------------");

        try {
            analyzeDirectory(logPath);
        } catch (IOException e) {
            System.err.println("Error reading logs: " + e.getMessage());
        }
    }

    private static void analyzeDirectory(Path startPath) throws IOException {
        Map<String, RequestData> requestMap = new HashMap<>();
        Map<String, String> threadToRequestMap = new HashMap<>();

        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".log")) {
                    processFile(file, requestMap, threadToRequestMap);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void processFile(Path file, Map<String, RequestData> requestMap, Map<String, String> threadToRequestMap) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, requestMap, threadToRequestMap);
            }
        } catch (IOException e) {
            System.err.println("Failed to read file: " + file);
        }
    }

    private static void parseLine(String line, Map<String, RequestData> requestMap, Map<String, String> threadToRequestMap) {
        Matcher baseMatcher = BASE_LOG_PATTERN.matcher(line);
        if (!baseMatcher.find()) return;

        String timestampStr = baseMatcher.group(1);
        String threadName = baseMatcher.group(2);
        String message = baseMatcher.group(4);

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(timestampStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return;
        }

        // L1: Start
        Matcher startMatcher = START_PATTERN.matcher(message);
        if (startMatcher.find()) {
            String requestId = startMatcher.group(1);
            RequestData data = new RequestData();
            data.requestId = requestId;
            data.startTime = timestamp;
            requestMap.put(requestId, data);
            threadToRequestMap.put(threadName, requestId);
            return;
        }

        // L2: Error
        Matcher errorMatcher = ERROR_PATTERN.matcher(message);
        if (errorMatcher.find()) {
            String requestId = errorMatcher.group(1);
            String errorReason = errorMatcher.group(2);
            RequestData data = requestMap.get(requestId);
            if (data != null) data.errorReason = errorReason;
            return;
        }

        // L3: End
        Matcher endMatcher = END_PATTERN.matcher(message);
        if (endMatcher.find()) {
            String requestId = threadToRequestMap.get(threadName);
            if (requestId != null) {
                RequestData data = requestMap.remove(requestId);
                threadToRequestMap.remove(threadName);
                if (data != null) {
                    long duration = ChronoUnit.MILLIS.between(data.startTime, timestamp);
                    printRow(data.requestId, duration, data.errorReason);
                }
            }
        }
    }

    private static void printRow(String requestId, long duration, String error) {
        String status = (error == null) ? "SUCCESS" : "FAILED";
        String cleanError = (error == null) ? "-" : error;
        if (cleanError.length() > 50) cleanError = cleanError.substring(0, 47) + "...";
        System.out.printf("%-36s | %-15d | %-10s | %s%n", requestId, duration, status, cleanError);
    }

    static class RequestData {
        String requestId;
        LocalDateTime startTime;
        String errorReason;
    }
}