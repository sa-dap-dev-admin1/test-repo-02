public interface TaxStrategy {
    double applyTax(double lineTotal, String country);
}

class DefaultTaxStrategy implements TaxStrategy {
    @Override
    public double applyTax(double lineTotal, String country) {
        switch (country) {
            case "IN":
                return lineTotal * 1.18; // 18% tax
            case "US":
                return lineTotal * 1.0825; // 8.25% tax
            default:
                return lineTotal * 1.1; // 10% tax
        }
    }
}