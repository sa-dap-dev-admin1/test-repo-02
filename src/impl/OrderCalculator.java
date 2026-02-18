import java.util.List;

public class OrderCalculator {
    private static final double IN_TAX_RATE = 0.18;
    private static final double US_TAX_RATE = 0.0825;
    private static final double DEFAULT_TAX_RATE = 0.1;
    private static final double DISCOUNT_RATE = 0.07;
    private static final double FIXED_DISCOUNT = 15;
    private static final int QUANTITY_DISCOUNT_THRESHOLD = 3;
    private static final double PRICE_DISCOUNT_THRESHOLD = 100;

    public static double calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double lineTotal = order.getPrice() * order.getQuantity();

        if (applyDiscount) {
            lineTotal -= calculateDiscount(order, lineTotal);
        }

        if (applyTax) {
            lineTotal += calculateTax(order.getCountry(), lineTotal);
        }

        return lineTotal;
    }

    public static double calculateRawTotal(List<Order> orders) {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }

    private static double calculateDiscount(Order order, double lineTotal) {
        if (order.getQuantity() > QUANTITY_DISCOUNT_THRESHOLD) {
            return lineTotal * DISCOUNT_RATE;
        } else if (order.getPrice() > PRICE_DISCOUNT_THRESHOLD) {
            return FIXED_DISCOUNT;
        }
        return 0;
    }

    private static double calculateTax(String country, double lineTotal) {
        switch (country) {
            case "IN":
                return lineTotal * IN_TAX_RATE;
            case "US":
                return lineTotal * US_TAX_RATE;
            default:
                return lineTotal * DEFAULT_TAX_RATE;
        }
    }
}