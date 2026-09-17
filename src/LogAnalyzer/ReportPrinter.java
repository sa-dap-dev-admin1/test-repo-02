package LogAnalyzer;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReportPrinter {

    public static void printFinalReport(Map<String, RequestData> requestMap) {
        List<RequestData> validRequests = filterRequests(requestMap);

        System.out.println("\n");
        System.out.println("=======================================================================================================================");
        System.out.println("FINAL ANALYSIS REPORT");
        System.out.println("Total Completed Requests: " + validRequests.size());
        System.out.println("=======================================================================================================================");

        printSummaryTable(validRequests);

        if (validRequests.isEmpty()) {
            System.out.println("\nNo completed requests found.");
            return;
        }

        printDetailedTable(validRequests);
    }

    private static List<RequestData> filterRequests(Map<String, RequestData> requestMap) {
        List<RequestData> validRequests = new ArrayList<>();
        int incompleteCount = 0;

        for (RequestData req : requestMap.values()) {
            boolean isFailed = (req.getErrorReason() != null);
            boolean isSuccess = req.isCompletedNormally();
            if (!isFailed && !isSuccess) {
                incompleteCount++;
            } else {
                validRequests.add(req);
            }
        }

        if (incompleteCount > 0) {
            System.out.println("[INFO] Skipped " + incompleteCount + " incomplete (unresolved) requests.");
        }

        return validRequests;
    }

    private static void printSummaryTable(List<RequestData> validRequests) {
        Map<String, Integer> summaryCounts = new HashMap<>();
        for (RequestData req : validRequests) {
            String cleanError = cleanErrorString(req.getErrorReason());
            summaryCounts.put(cleanError, summaryCounts.getOrDefault(cleanError, 0) + 1);
        }

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

    private static void printDetailedTable(List<RequestData> validRequests) {
        validRequests.sort((r1, r2) -> {
            String e1 = (r1.getErrorReason() == null) ? "ZZZZ_SUCCESS" : r1.getErrorReason();
            String e2 = (r2.getErrorReason() == null) ? "ZZZZ_SUCCESS" : r2.getErrorReason();
            int errorCompare = e1.compareTo(e2);
            if (errorCompare != 0) return errorCompare;

            if (r1.getStartTime() == null && r2.getStartTime() == null) return 0;
            if (r1.getStartTime() == null) return 1;
            if (r2.getStartTime() == null) return -1;
            return r1.getStartTime().compareTo(r2.getStartTime());
        });

        System.out.println("\n--- DETAILED REQUESTS ---");
        System.out.printf("%-36s | %-12s | %-12s | %-10s | %s%n",
            "Request ID", "Duration(s)", "Status", "Confidence", "Error Reason");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        for (RequestData req : validRequests) {
            String status = "UNKNOWN";
            if (req.getErrorReason() != null) status = "FAILED";
            else if (req.isCompletedNormally()) status = "SUCCESS";

            double durationSeconds = 0.0;
            if (req.getStartTime() != null && req.getEndTime() != null) {
                long durationMillis = ChronoUnit.MILLIS.between(req.getStartTime(), req.getEndTime());
                durationSeconds = durationMillis / 1000.0;
            }

            String conf = (req.getConfidence() != null) ? req.getConfidence() : "-";
            String cleanError = cleanErrorString(req.getErrorReason());
            if (cleanError.length() > 50) cleanError = cleanError.substring(0, 47) + "...";

            System.out.printf("%-36s | %-12.2f | %-12s | %-10s | %s%n",
                req.getRequestId(), durationSeconds, status, conf, cleanError);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    private static String cleanErrorString(String errorReason) {
        if (errorReason == null) return "-";
        String cleaned = errorReason;
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}
