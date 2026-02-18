public class TaxCalculator {
    public double applyTax(double amount, String country) {
        double taxRate = switch (country) {
            case "IN" -> 0.18;
            case "US" -> 0.0825;
            default -> 0.1;
        };
        return amount * (1 + taxRate);
    }
}