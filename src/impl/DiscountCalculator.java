public interface DiscountCalculator {
    double applyDiscount(Order order, double amount);
}

class DiscountCalculatorImpl implements DiscountCalculator {
    @Override
    public double applyDiscount(Order order, double amount) {
        if (order.getQuantity() > 3) {
            return amount * 0.93; // 7% discount
        } else if (order.getPrice() > 100) {
            return amount - 15; // fixed discount
        }
        return amount;
    }
}

class DiscountCalculatorFactory {
    public DiscountCalculator createDiscountCalculator() {
        return new DiscountCalculatorImpl();
    }
}