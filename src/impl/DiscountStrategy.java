public interface DiscountStrategy {
    double applyDiscount(double lineTotal, int quantity, double price);
}

class DefaultDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double lineTotal, int quantity, double price) {
        if (quantity > 3) {
            return lineTotal * 0.93; // 7% discount
        } else if (price > 100) {
            return lineTotal - 15; // fixed discount
        }
        return lineTotal;
    }
}