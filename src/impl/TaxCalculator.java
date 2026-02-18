public interface TaxCalculator {
    double calculateTax(Order order, double amount);
}

class TaxCalculatorImpl implements TaxCalculator {
    @Override
    public double calculateTax(Order order, double amount) {
        switch (order.getCountry()) {
            case "IN":
                return amount * 1.18;
            case "US":
                return amount * 1.0825;
            default:
                return amount * 1.1;
        }
    }
}

class TaxCalculatorFactory {
    public TaxCalculator createTaxCalculator() {
        return new TaxCalculatorImpl();
    }
}