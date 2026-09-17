package loganalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class LogAnalyzer {

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, "Desktop", "integrator-server-logs");

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
        LogFileCollector collector = new LogFileCollector();
        List<Path> logFiles = collector.collectSortedFiles(startPath);

        System.out.println("[INFO] Processing " + logFiles.size() + " files.");

        Map<String, RequestData> allRequests = new HashMap<>();
        Map<String, String> threadToRequestMap = new HashMap<>();
        LinkedList<String> pendingQueue = new LinkedList<>();

        LogLineParser parser = new LogLineParser(allRequests, threadToRequestMap, pendingQueue);

        for (Path file : logFiles) {
            System.out.println("   -> Reading: " + file.getFileName());
            processFile(file, parser);
        }

        LogReportPrinter printer = new LogReportPrinter();
        printer.printReport(allRequests);
    }

    private static void processFile(Path file, LogLineParser parser) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parser.parseLine(line);
            }
        } catch (IOException e) {
            System.err.println("   [ERROR] Failed to read file: " + e.getMessage());
        }
    }
}
