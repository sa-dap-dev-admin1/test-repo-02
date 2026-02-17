import java.util.List;

public class OrderReporter {
    public String generateReport(List<Order> orders) {
        StringBuilder report = new StringBuilder();
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            if (order instanceof ProcessedOrder) {
                ProcessedOrder processedOrder = (ProcessedOrder) order;
                report.append(formatOrderLine(processedOrder));
                grandTotal += processedOrder.getLineTotal();
                if (processedOrder.isSuspicious()) {
                    suspiciousCount++;
                }
            }
        }

        report.append("RAW_TOTAL=").append(calculateRawTotal(orders)).append("\n");
        report.append("GRAND_TOTAL=").append(grandTotal).append("\n");
        report.append("SUSPICIOUS=").append(suspiciousCount).append("\n");

        return report.toString();
    }

    public double calculateRawTotal(List<Order> orders) {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }

    private String formatOrderLine(ProcessedOrder order) {
        return String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                order.getProcessDate(), order.getId(), order.getQuantity(),
                order.getCountry(), order.getLineTotal());
    }
}