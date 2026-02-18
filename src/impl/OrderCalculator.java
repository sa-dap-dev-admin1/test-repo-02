public class OrderCalculator {
    private static final double QUANTITY_DISCOUNT_THRESHOLD = 3;
    private static final double QUANTITY_DISCOUNT_RATE = 0.07;
    private static final double PRICE_DISCOUNT_THRESHOLD = 100;
    private static final double PRICE_DISCOUNT_AMOUNT = 15;

    private static final double INDIA_TAX_RATE = 0.18;
    private static final double US_TAX_RATE = 0.0825;
    private static final double DEFAULT_TAX_RATE = 0.1;

    public ProcessedOrder calculateOrderTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double total = order.getPrice() * order.getQuantity();

        if (applyDiscount) {
            total = applyDiscount(total, order.getQuantity(), order.getPrice());
        }

        if (applyTax) {
            total = applyTax(total, order.getCountry());
        }

        return new ProcessedOrder(order, total);
    }

    private double applyDiscount(double total, int quantity, double price) {
        if (quantity > QUANTITY_DISCOUNT_THRESHOLD) {
            return total * (1 - QUANTITY_DISCOUNT_RATE);
        } else if (price > PRICE_DISCOUNT_THRESHOLD) {
            return total - PRICE_DISCOUNT_AMOUNT;
        }
        return total;
    }

    private double applyTax(double total, String country) {
        double taxRate = switch (country) {
            case "IN" -> INDIA_TAX_RATE;
            case "US" -> US_TAX_RATE;
            default -> DEFAULT_TAX_RATE;
        };
        return total * (1 + taxRate);
    }
}