public class OrderCalculator {
    public double calculateLineTotal(OrderDTO order, boolean applyTax, boolean applyDiscount) {
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
        if (quantity > OrderConstants.QUANTITY_DISCOUNT_THRESHOLD) {
            return lineTotal * (1 - OrderConstants.QUANTITY_DISCOUNT_RATE);
        } else if (price > OrderConstants.PRICE_DISCOUNT_THRESHOLD) {
            return lineTotal - OrderConstants.FIXED_DISCOUNT;
        }
        return lineTotal;
    }

    private double applyTax(double lineTotal, String country) {
        double taxRate = OrderConstants.DEFAULT_TAX_RATE;
        if ("IN".equals(country)) {
            taxRate = OrderConstants.IN_TAX_RATE;
        } else if ("US".equals(country)) {
            taxRate = OrderConstants.US_TAX_RATE;
        }
        return lineTotal * (1 + taxRate);
    }
}