import java.util.List;
import static OrderModel.*;

public class OrderReporter {
    private final boolean verbose;

    public OrderReporter(boolean verbose) {
        this.verbose = verbose;
    }

    public String generateReport(List<ProcessedOrder> processedOrders) {
        StringBuilder report = new StringBuilder();
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (ProcessedOrder processedOrder : processedOrders) {
            Order order = processedOrder.getOrder();
            double lineTotal = processedOrder.getLineTotal();

            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    order.getTimestamp(), order.getId(), order.getQuantity(), order.getCountry(), lineTotal));

            grandTotal += lineTotal;
            if (processedOrder.isSuspicious()) {
                suspiciousCount++;
            }

            if (verbose) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        report.append(String.format("RAW_TOTAL=%.2f%n", calculateRawTotal(processedOrders)));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));

        return report.toString();
    }

    private double calculateRawTotal(List<ProcessedOrder> processedOrders) {
        return processedOrders.stream()
                .mapToDouble(po -> po.getOrder().getPrice() * po.getOrder().getQuantity())
                .sum();
    }
}