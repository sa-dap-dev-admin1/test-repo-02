import java.util.*;

public class OrderCalculator {
    private static final List<Order> orders = new ArrayList<>();

    public static void addOrder(String id, int quantity, double price, String country) {
        orders.add(new Order(id, quantity, price, country));
    }

    public String process(String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        StringBuilder output = new StringBuilder();
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            double lineTotal = calculateLineTotal(order, applyTax, applyDiscount);
            grandTotal += lineTotal;

            output.append(formatOrderLine(date, order, lineTotal)).append("\n");

            if (OrderValidator.isSuspicious(order, riskFlag)) {
                suspiciousCount++;
            }

            logProcessedOrder(order.getId(), lineTotal);
        }

        appendTotals(output, grandTotal, suspiciousCount);

        return output.toString();
    }

    private double calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double lineTotal = order.getPrice() * order.getQuantity();

        if (applyDiscount) {
            lineTotal = applyDiscount(lineTotal, order);
        }

        if (applyTax) {
            lineTotal = applyTax(lineTotal, order.getCountry());
        }

        return lineTotal;
    }

    private double applyDiscount(double lineTotal, Order order) {
        if (order.getQuantity() > 3) {
            return lineTotal * 0.93; // 7% discount
        } else if (order.getPrice() > 100) {
            return lineTotal - 15; // fixed discount
        }
        return lineTotal;
    }

    private double applyTax(double lineTotal, String country) {
        switch (country) {
            case "IN":
                return lineTotal * 1.18; // 18% tax
            case "US":
                return lineTotal * 1.0825; // 8.25% tax
            default:
                return lineTotal * 1.1; // 10% tax
        }
    }

    private String formatOrderLine(String date, Order order, double lineTotal) {
        return String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f",
                date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal);
    }

    private void appendTotals(StringBuilder output, double grandTotal, int suspiciousCount) {
        output.append("RAW_TOTAL=").append(calculateRawTotal()).append("\n");
        output.append("GRAND_TOTAL=").append(grandTotal).append("\n");
        output.append("SUSPICIOUS=").append(suspiciousCount).append("\n");
    }

    private void logProcessedOrder(String id, double lineTotal) {
        if (Configuration.isDebugMode()) {
            System.err.println("Processed: " + id + " -> " + lineTotal);
        }
    }

    public static double calculateRawTotal() {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}