import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderProcessor {
    private List<OrderDTO> orders;
    private int mode;

    public OrderProcessor() {
        this.orders = new CopyOnWriteArrayList<>();
        this.mode = 0;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void addOrder(String id, int quantity, double price, String country) {
        OrderDTO order = new OrderDTO(id, quantity, price, country, System.currentTimeMillis());
        orders.add(order);
    }

    public String processOrders(String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        OrderCalculator calculator = new OrderCalculator();
        OrderValidator validator = new OrderValidator();
        ReportGenerator reportGenerator = new ReportGenerator();

        double grandTotal = 0;
        int suspiciousCount = 0;

        for (OrderDTO order : orders) {
            double lineTotal = calculator.calculateLineTotal(order, applyTax, applyDiscount);
            grandTotal += lineTotal;

            if (validator.isSuspicious(order, riskFlag)) {
                suspiciousCount++;
            }

            if (mode == 1) {
                LoggingUtil.log("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        try {
            if (grandTotal > OrderConstants.MAX_GRAND_TOTAL) {
                throw new OrderProcessingException("Total exceeds maximum allowed");
            }
        } catch (OrderProcessingException e) {
            LoggingUtil.logError("Error processing orders: " + e.getMessage());
        }

        return reportGenerator.generateReport(orders, date, grandTotal, suspiciousCount);
    }

    public double calculateRawTotal() {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}