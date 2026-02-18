import java.util.ArrayList;
import java.util.List;

public class OrderReport {
    private String date;
    private List<OrderLine> orderLines;
    private int suspiciousCount;
    private double grandTotal;
    private double rawTotal;

    public OrderReport(String date) {
        this.date = date;
        this.orderLines = new ArrayList<>();
        this.suspiciousCount = 0;
        this.grandTotal = 0;
        this.rawTotal = 0;
    }

    public void addOrderLine(Order order, double lineTotal) {
        orderLines.add(new OrderLine(order, lineTotal));
        grandTotal += lineTotal;
        rawTotal += order.getPrice() * order.getQuantity();
    }

    public void incrementSuspiciousCount() {
        suspiciousCount++;
    }

    public String generateReport(boolean isVerboseMode) {
        StringBuilder report = new StringBuilder();

        for (OrderLine line : orderLines) {
            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    date, line.getOrder().getId(), line.getOrder().getQuantity(),
                    line.getOrder().getCountry(), line.getLineTotal()));

            if (isVerboseMode) {
                System.err.println("Processed: " + line.getOrder().getId() + " -> " + line.getLineTotal());
            }
        }

        report.append(String.format("RAW_TOTAL=%.2f%n", rawTotal));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));

        return report.toString();
    }

    public double getRawTotal() {
        return rawTotal;
    }

    private static class OrderLine {
        private Order order;
        private double lineTotal;

        public OrderLine(Order order, double lineTotal) {
            this.order = order;
            this.lineTotal = lineTotal;
        }

        public Order getOrder() {
            return order;
        }

        public double getLineTotal() {
            return lineTotal;
        }
    }
}