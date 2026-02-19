public class OrderCalculator {
    private static final double QUANTITY_DISCOUNT_THRESHOLD = 3;
    private static final double QUANTITY_DISCOUNT_RATE = 0.07;
    private static final double PRICE_DISCOUNT_THRESHOLD = 100;
    private static final double PRICE_DISCOUNT_AMOUNT = 15;
    private static final double INDIA_TAX_RATE = 0.18;
    private static final double US_TAX_RATE = 0.0825;
    private static final double DEFAULT_TAX_RATE = 0.1;

    public double calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double lineTotal = order.getPrice() * order.getQuantity();

        if (applyDiscount) {
            lineTotal -= calculateDiscount(order, lineTotal);
        }

        if (applyTax) {
            lineTotal += calculateTax(order, lineTotal);
        }

        return lineTotal;
    }

    private double calculateDiscount(Order order, double lineTotal) {
        if (order.getQuantity() > QUANTITY_DISCOUNT_THRESHOLD) {
            return lineTotal * QUANTITY_DISCOUNT_RATE;
        } else if (order.getPrice() > PRICE_DISCOUNT_THRESHOLD) {
            return PRICE_DISCOUNT_AMOUNT;
        }
        return 0;
    }

    private double calculateTax(Order order, double lineTotal) {
        switch (order.getCountry()) {
            case "IN":
                return lineTotal * INDIA_TAX_RATE;
            case "US":
                return lineTotal * US_TAX_RATE;
            default:
                return lineTotal * DEFAULT_TAX_RATE;
        }
    }
}