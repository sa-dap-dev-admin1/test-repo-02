package LogAnalyzer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class LogAnalyzer {

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        // Default path - update this to point to your log directory
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
            List<Path> logFiles = LogFileCollector.collect(logPath);
            System.out.println("[INFO] Processing " + logFiles.size() + " files.");

            Map<String, RequestData> allRequests = new HashMap<>();
            Map<String, String> threadToRequestMap = new HashMap<>();
            LinkedList<String> pendingQueue = new LinkedList<>();

            for (Path file : logFiles) {
                System.out.println("   -> Reading: " + file.getFileName());
                LogLineParser.processFile(file, allRequests, threadToRequestMap, pendingQueue);
            }

            ReportPrinter.printFinalReport(allRequests);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
