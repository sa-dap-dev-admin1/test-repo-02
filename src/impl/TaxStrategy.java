import java.math.BigDecimal;

public interface TaxStrategy {
    BigDecimal applyTax(Order order, BigDecimal amount);
}

class DefaultTaxStrategy implements TaxStrategy {
    @Override
    public BigDecimal applyTax(Order order, BigDecimal amount) {
        switch (order.getCountry()) {
            case "IN":
                return amount.multiply(BigDecimal.valueOf(1.18)); // 18% tax
            case "US":
                return amount.multiply(BigDecimal.valueOf(1.0825)); // 8.25% tax
            default:
                return amount.multiply(BigDecimal.valueOf(1.1)); // 10% tax
        }
    }
}