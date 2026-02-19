public class DiscountCalculator {
    private static final int QUANTITY_THRESHOLD = 3;
    private static final double QUANTITY_DISCOUNT_RATE = 0.07;
    private static final double PRICE_THRESHOLD = 100;
    private static final double FIXED_DISCOUNT = 15;

    public double applyDiscount(double lineTotal, int quantity, double price) {
        if (quantity > QUANTITY_THRESHOLD) {
            return lineTotal * (1 - QUANTITY_DISCOUNT_RATE);
        } else if (price > PRICE_THRESHOLD) {
            return lineTotal - FIXED_DISCOUNT;
        }
        return lineTotal;
    }
}