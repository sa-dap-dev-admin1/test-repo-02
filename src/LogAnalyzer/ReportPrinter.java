package LogAnalyzer;

import java.time.temporal.ChronoUnit;
import java.util.*;

class ReportPrinter {

    static void printFinalReport(Map<String, LogAnalyzer.RequestData> requestMap) {
        List<LogAnalyzer.RequestData> validRequests = filterCompletedRequests(requestMap);
        Map<String, Integer> summaryCounts = buildSummaryCounts(validRequests);
        printReportHeader(validRequests.size());
        printSummaryTable(summaryCounts);

        if (validRequests.isEmpty()) {
            System.out.println("\nNo completed requests found.");
            return;
        }

        validRequests.sort(ReportPrinter::compareRequests);
        printDetailTable(validRequests);
    }

    private static List<LogAnalyzer.RequestData> filterCompletedRequests(
            Map<String, LogAnalyzer.RequestData> requestMap) {
        List<LogAnalyzer.RequestData> validRequests = new ArrayList<>();
        for (LogAnalyzer.RequestData req : requestMap.values()) {
            if (req.errorReason != null || req.completedNormally) {
                validRequests.add(req);
            }
        }
        return validRequests;
    }

    private static Map<String, Integer> buildSummaryCounts(List<LogAnalyzer.RequestData> validRequests) {
        Map<String, Integer> summaryCounts = new HashMap<>();
        for (LogAnalyzer.RequestData req : validRequests) {
            String key = normalizeError(req.errorReason);
            summaryCounts.put(key, summaryCounts.getOrDefault(key, 0) + 1);
        }
        return summaryCounts;
    }

    private static String normalizeError(String errorReason) {
        String e = (errorReason == null) ? "-" : errorReason;
        if (e.startsWith("[") && e.endsWith("]")) return e.substring(1, e.length() - 1);
        return e;
    }

    private static void printReportHeader(int totalCount) {
        System.out.println("\n");
        System.out.println("=======================================================================================================================");
        System.out.println("FINAL ANALYSIS REPORT");
        System.out.println("Total Completed Requests: " + totalCount);
        System.out.println("=======================================================================================================================");
    }

    private static void printSummaryTable(Map<String, Integer> summaryCounts) {
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
    }

    private static int compareRequests(LogAnalyzer.RequestData r1, LogAnalyzer.RequestData r2) {
        String e1 = (r1.errorReason == null) ? "ZZZZ_SUCCESS" : r1.errorReason;
        String e2 = (r2.errorReason == null) ? "ZZZZ_SUCCESS" : r2.errorReason;
        int errorCompare = e1.compareTo(e2);
        if (errorCompare != 0) return errorCompare;

        // --- FIX FOR IllegalArgumentException ---
        // Must strictly handle all null permutations to preserve the comparison contract
        if (r1.startTime == null && r2.startTime == null) return 0;
        if (r1.startTime == null) return 1;
        if (r2.startTime == null) return -1;
        // ----------------------------------------

        return r1.startTime.compareTo(r2.startTime);
    }

    private static void printDetailTable(List<LogAnalyzer.RequestData> validRequests) {
        System.out.println("\n--- DETAILED REQUESTS ---");
        System.out.printf("%-36s | %-12s | %-12s | %-10s | %s%n",
            "Request ID", "Duration(s)", "Status", "Confidence", "Error Reason");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        for (LogAnalyzer.RequestData req : validRequests) {
            printDetailRow(req);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    private static void printDetailRow(LogAnalyzer.RequestData req) {
        String status = deriveStatus(req);
        double durationSeconds = computeDuration(req);
        String conf = (req.confidence != null) ? req.confidence : "-";
        String cleanError = normalizeError(req.errorReason);
        if (cleanError.length() > 50) cleanError = cleanError.substring(0, 47) + "...";

        System.out.printf("%-36s | %-12.2f | %-12s | %-10s | %s%n",
            req.requestId, durationSeconds, status, conf, cleanError);
    }

    private static String deriveStatus(LogAnalyzer.RequestData req) {
        if (req.errorReason != null) return "FAILED";
        if (req.completedNormally) return "SUCCESS";
        return "UNKNOWN";
    }

    private static double computeDuration(LogAnalyzer.RequestData req) {
        if (req.startTime == null || req.endTime == null) return 0.0;
        return ChronoUnit.MILLIS.between(req.startTime, req.endTime) / 1000.0;
    }
}
