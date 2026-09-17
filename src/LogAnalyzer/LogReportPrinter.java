package loganalyzer;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class LogReportPrinter {

    public void printReport(Map<String, RequestData> requestMap) {
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

        Map<String, Integer> summaryCounts = buildSummary(validRequests);

        printHeader(validRequests.size());
        printSummarySection(summaryCounts);

        if (validRequests.isEmpty()) {
            System.out.println("\nNo completed requests found.");
            return;
        }

        sortDetailRows(validRequests);
        printDetailRows(validRequests);
    }

    private Map<String, Integer> buildSummary(List<RequestData> validRequests) {
        Map<String, Integer> summaryCounts = new HashMap<>();
        for (RequestData req : validRequests) {
            String cleanError = cleanErrorReason(req.errorReason);
            summaryCounts.put(cleanError, summaryCounts.getOrDefault(cleanError, 0) + 1);
        }
        return summaryCounts;
    }

    private void printHeader(int totalCompleted) {
        System.out.println("\n");
        System.out.println("=======================================================================================================================");
        System.out.println("FINAL ANALYSIS REPORT");
        System.out.println("Total Completed Requests: " + totalCompleted);
        System.out.println("=======================================================================================================================");
    }

    private void printSummarySection(Map<String, Integer> summaryCounts) {
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

    private void sortDetailRows(List<RequestData> validRequests) {
        validRequests.sort((r1, r2) -> {
            String e1 = (r1.errorReason == null) ? "ZZZZ_SUCCESS" : r1.errorReason;
            String e2 = (r2.errorReason == null) ? "ZZZZ_SUCCESS" : r2.errorReason;
            int errorCompare = e1.compareTo(e2);
            if (errorCompare != 0) return errorCompare;

            if (r1.startTime == null && r2.startTime == null) return 0;
            if (r1.startTime == null) return 1;
            if (r2.startTime == null) return -1;

            return r1.startTime.compareTo(r2.startTime);
        });
    }

    private void printDetailRows(List<RequestData> validRequests) {
        System.out.println("\n--- DETAILED REQUESTS ---");
        System.out.printf("%-36s | %-12s | %-12s | %-10s | %s%n",
            "Request ID", "Duration(s)", "Status", "Confidence", "Error Reason");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        for (RequestData req : validRequests) {
            String status = determineStatus(req);
            double durationSeconds = calculateDuration(req);
            String conf = (req.confidence != null) ? req.confidence : "-";
            String cleanError = cleanErrorReason(req.errorReason);
            if (cleanError.length() > 50) cleanError = cleanError.substring(0, 47) + "...";

            System.out.printf("%-36s | %-12.2f | %-12s | %-10s | %s%n",
                req.requestId, durationSeconds, status, conf, cleanError);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    private String determineStatus(RequestData req) {
        if (req.errorReason != null) return "FAILED";
        if (req.completedNormally) return "SUCCESS";
        return "UNKNOWN";
    }

    private double calculateDuration(RequestData req) {
        if (req.startTime != null && req.endTime != null) {
            long durationMillis = ChronoUnit.MILLIS.between(req.startTime, req.endTime);
            return durationMillis / 1000.0;
        }
        return 0.0;
    }

    private String cleanErrorReason(String errorReason) {
        if (errorReason == null) return "-";
        String cleaned = errorReason;
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}
