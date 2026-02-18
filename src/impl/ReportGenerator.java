import java.util.List;

public class ReportGenerator {
    public String generateReport(List<ProcessedOrder> processedOrders, String date, double grandTotal, int suspiciousCount) {
        StringBuilder report = new StringBuilder();
        double rawTotal = 0;

        for (ProcessedOrder processedOrder : processedOrders) {
            Order order = processedOrder.getOrder();
            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    date, order.getId(), order.getQuantity(), order.getCountry(), processedOrder.getTotal()));
            rawTotal += order.getPrice() * order.getQuantity();
        }

        report.append(String.format("RAW_TOTAL=%.2f%n", rawTotal));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));

        return report.toString();
    }
}