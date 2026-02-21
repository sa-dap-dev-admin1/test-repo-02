import java.util.List;

public class ReportGenerator {
    public String generateOrderLine(Order order, String date, double lineTotal) {
        return String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal);
    }

    public String generateFinalReport(List<String> processedOrders, double grandTotal, double rawTotal, int suspiciousCount) {
        StringBuilder report = new StringBuilder();
        processedOrders.forEach(report::append);
        report.append(String.format("RAW_TOTAL=%.2f%n", rawTotal));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));
        return report.toString();
    }
}