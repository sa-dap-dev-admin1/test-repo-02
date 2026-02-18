public class DiscountCalculator {
    public double applyDiscount(double amount, int quantity, double price) {
        if (quantity > 3) {
            return amount * 0.93; // 7% discount
        } else if (price > 100) {
            return amount - 15; // fixed discount
        }
        return amount;
    }
}