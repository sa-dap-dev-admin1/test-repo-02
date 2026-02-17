import java.math.BigDecimal;
import java.util.List;

public class ReportGenerator {
    public String generateReport(List<Order> orders) {
        StringBuilder report = new StringBuilder();
        BigDecimal grandTotal = BigDecimal.ZERO;
        int suspiciousCount = 0;

        for (Order order : orders) {
            if (order instanceof ProcessedOrder) {
                ProcessedOrder processedOrder = (ProcessedOrder) order;
                report.append(generateOrderLine(processedOrder));
                grandTotal = grandTotal.add(processedOrder.getLineTotal());
                if (processedOrder.isSuspicious()) {
                    suspiciousCount++;
                }
            }
        }

        BigDecimal rawTotal = calculateRawTotal(orders);

        report.append("RAW_TOTAL=").append(rawTotal).append("\n");
        report.append("GRAND_TOTAL=").append(grandTotal).append("\n");
        report.append("SUSPICIOUS=").append(suspiciousCount).append("\n");

        return report.toString();
    }

    private String generateOrderLine(ProcessedOrder order) {
        return String.format("date=%s, id=%s, qty=%d, country=%s, line=%s%n",
                order.getProcessDate(),
                order.getId(),
                order.getQuantity(),
                order.getCountry(),
                order.getLineTotal());
    }

    private BigDecimal calculateRawTotal(List<Order> orders) {
        return orders.stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}