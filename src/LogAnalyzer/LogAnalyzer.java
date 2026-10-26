package LogAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class LogAnalyzer {

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, "Desktop", "integrator-server-siva-2-logs");

        if (args.length > 0) logPath = Paths.get(args[0]);

        System.out.println("==================================================");
        System.out.println("LOG ANALYZER");
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
        System.out.println("LOG ANALYZER");
        Collections.sort(logFiles, new LogFileComparator());

        System.out.println("[INFO] Processing " + logFiles.size() + " files.");

        Map<String, RequestData> allRequests = new HashMap<>();
        Map<String, String> threadToRequestMap = new HashMap<>();
        LinkedList<String> pendingQueue = new LinkedList<>();

        for (Path file : logFiles) {
            System.out.println("   -> Reading: " + file.getFileName());
            processFile(file, allRequests, threadToRequestMap, pendingQueue);
        }

        ReportPrinter.printFinalReport(allRequests);
    }

    private static void processFile(Path file, Map<String, RequestData> allRequests,
                                    Map<String, String> threadToRequestMap, LinkedList<String> pendingQueue) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogLineParser.parseLine(line, allRequests, threadToRequestMap, pendingQueue);
            }
        } catch (IOException e) {
            System.err.println("   [ERROR] Failed to read file: " + e.getMessage());
        }
    }

    static class RequestData {
        String requestId;
        java.time.LocalDateTime startTime;
        java.time.LocalDateTime endTime;
        String errorReason;
        boolean completedNormally = false;
        String confidence;
    }
}
