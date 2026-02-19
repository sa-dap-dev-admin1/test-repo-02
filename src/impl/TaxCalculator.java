public class TaxCalculator {
    private static final double INDIA_TAX_RATE = 0.18;
    private static final double US_TAX_RATE = 0.0825;
    private static final double DEFAULT_TAX_RATE = 0.1;

    public double applyTax(double amount, String country) {
        double taxRate = getTaxRate(country);
        return amount * (1 + taxRate);
    }

    private double getTaxRate(String country) {
        switch (country) {
            case "IN":
                return INDIA_TAX_RATE;
            case "US":
                return US_TAX_RATE;
            default:
                return DEFAULT_TAX_RATE;
        }
    }
}