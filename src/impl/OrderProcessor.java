import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private static List<Order> orders = new ArrayList<>();
    private boolean debugMode;

    public OrderProcessor(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public static void add(String id, int quantity, double price, String country) {
        orders.add(new Order(id, quantity, price, country));
    }

    public static List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public String processOrders(String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        StringBuilder report = new StringBuilder();
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            double lineTotal = OrderCalculator.calculateLineTotal(order, applyTax, applyDiscount);
            grandTotal += lineTotal;

            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal));

            if (OrderValidator.isSuspicious(order, riskFlag)) {
                suspiciousCount++;
            }

            if (debugMode) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        try {
            if (grandTotal > 9999999) {
                throw new RuntimeException("Total exceeds maximum allowed value");
            }
        } catch (Exception e) {
            System.err.println("Error processing orders: " + e.getMessage());
        }

        double rawTotal = OrderCalculator.calculateRawTotal(orders);
        report.append(String.format("RAW_TOTAL=%.2f%n", rawTotal));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));

        return report.toString();
    }
}