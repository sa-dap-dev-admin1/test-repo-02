import java.util.ArrayList;
import java.util.List;
import static OrderModel.*;

public class OrderCalculator {
    private static final double DISCOUNT_THRESHOLD_QUANTITY = 3;
    private static final double DISCOUNT_THRESHOLD_PRICE = 100;
    private static final double DISCOUNT_RATE = 0.07;
    private static final double FIXED_DISCOUNT = 15;
    private static final double TAX_RATE_IN = 0.18;
    private static final double TAX_RATE_US = 0.0825;
    private static final double TAX_RATE_OTHER = 0.1;
    private static final double SUSPICIOUS_AMOUNT = 300;

    public List<ProcessedOrder> processOrders(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        List<ProcessedOrder> processedOrders = new ArrayList<>();

        for (Order order : orders) {
            double lineTotal = calculateLineTotal(order, applyTax, applyDiscount);
            boolean suspicious = assessRisk(order, riskFlag);
            processedOrders.add(new ProcessedOrder(order, lineTotal, suspicious));
        }

        return processedOrders;
    }

    private double calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double lineTotal = order.getPrice() * order.getQuantity();

        if (applyDiscount) {
            lineTotal = applyDiscount(lineTotal, order.getQuantity(), order.getPrice());
        }

        if (applyTax) {
            lineTotal = applyTax(lineTotal, order.getCountry());
        }

        return lineTotal;
    }

    private double applyDiscount(double lineTotal, int quantity, double price) {
        if (quantity > DISCOUNT_THRESHOLD_QUANTITY) {
            return lineTotal * (1 - DISCOUNT_RATE);
        } else if (price > DISCOUNT_THRESHOLD_PRICE) {
            return lineTotal - FIXED_DISCOUNT;
        }
        return lineTotal;
    }

    private double applyTax(double lineTotal, Country country) {
        switch (country) {
            case IN:
                return lineTotal * (1 + TAX_RATE_IN);
            case US:
                return lineTotal * (1 + TAX_RATE_US);
            default:
                return lineTotal * (1 + TAX_RATE_OTHER);
        }
    }

    private boolean assessRisk(Order order, int riskFlag) {
        if (riskFlag > 5) {
            return (order.getQuantity() * order.getPrice() > SUSPICIOUS_AMOUNT) || 
                   (order.getId() != null && order.getId().startsWith("D"));
        } else {
            return order.getQuantity() == 0;
        }
    }

    public double calculateRawTotal(List<Order> orders) {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}