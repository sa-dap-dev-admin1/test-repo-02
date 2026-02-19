import java.util.*;

public class OrderService {
    private List<Order> orders;
    private boolean mode;
    private OrderCalculator calculator;
    private RiskAssessor riskAssessor;

    public OrderService() {
        this.orders = new ArrayList<>();
        this.calculator = new OrderCalculator();
        this.riskAssessor = new RiskAssessor();
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public void addOrder(String id, int qty, double price, String country) {
        Order order = new Order(id, qty, price, country);
        orders.add(order);
    }

    public String processOrders(String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        StringBuilder report = new StringBuilder();
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            double lineTotal = calculator.calculateLineTotal(order, applyTax, applyDiscount);
            grandTotal += lineTotal;

            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal));

            if (riskAssessor.isOrderSuspicious(order, riskFlag)) {
                suspiciousCount++;
            }

            if (mode) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        try {
            if (grandTotal > 9999999) {
                throw new RuntimeException("Total too large");
            }
        } catch (RuntimeException e) {
            System.err.println("Error processing orders: " + e.getMessage());
        }

        double rawTotal = calculateTotal();
        report.append(String.format("RAW_TOTAL=%.2f%n", rawTotal));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));

        return report.toString();
    }

    public double calculateTotal() {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}