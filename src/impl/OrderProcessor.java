import java.util.*;

public class OrderProcessor {
    private List<Order> orders;
    private boolean verbose;
    private OrderCalculator calculator;
    private OrderValidator validator;
    private ReportGenerator reportGenerator;

    public OrderProcessor(boolean verbose) {
        this.orders = new ArrayList<>();
        this.verbose = verbose;
        this.calculator = new OrderCalculator();
        this.validator = new OrderValidator();
        this.reportGenerator = new ReportGenerator();
    }

    public void addOrder(String id, int quantity, double price, String country) {
        orders.add(new Order(id, quantity, price, country));
    }

    public String processOrders(String date, boolean applyTax, boolean applyDiscount, int riskThreshold) {
        List<ProcessedOrder> processedOrders = new ArrayList<>();
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            ProcessedOrder processedOrder = calculator.calculateOrderTotal(order, applyTax, applyDiscount);
            grandTotal += processedOrder.getTotal();

            if (validator.isSuspicious(processedOrder, riskThreshold)) {
                suspiciousCount++;
            }

            processedOrders.add(processedOrder);

            if (verbose) {
                System.err.println("Processed: " + order.getId() + " -> " + processedOrder.getTotal());
            }
        }

        return reportGenerator.generateReport(processedOrders, date, grandTotal, suspiciousCount);
    }

    public double calculateTotalAmount() {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}