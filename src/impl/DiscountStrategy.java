import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal applyDiscount(Order order, BigDecimal amount);
}

class DefaultDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(Order order, BigDecimal amount) {
        if (order.getQuantity() > 3) {
            return amount.multiply(BigDecimal.valueOf(0.93)); // 7% discount
        } else if (order.getPrice().compareTo(BigDecimal.valueOf(100)) > 0) {
            return amount.subtract(BigDecimal.valueOf(15)); // $15 fixed discount
        }
        return amount;
    }
}