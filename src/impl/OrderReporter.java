import java.util.ArrayList;
import java.util.List;

public class OrderReporter {
    private List<String> reportLines = new ArrayList<>();

    public void addOrderToReport(String date, Order order, double lineTotal) {
        reportLines.add(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f",
                date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal));
    }

    public String generateReport(List<Order> orders, double grandTotal, int suspiciousCount) {
        StringBuilder report = new StringBuilder();
        for (String line : reportLines) {
            report.append(line).append("\n");
        }
        report.append("RAW_TOTAL=").append(calculateRawTotal(orders)).append("\n");
        report.append("GRAND_TOTAL=").append(grandTotal).append("\n");
        report.append("SUSPICIOUS=").append(suspiciousCount).append("\n");
        return report.toString();
    }

    private double calculateRawTotal(List<Order> orders) {
        return orders.stream().mapToDouble(Order::calculateLineTotal).sum();
    }
}