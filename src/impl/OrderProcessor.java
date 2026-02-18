import java.util.List;

public class OrderProcessor {
    private final boolean verbose;
    private final TaxCalculator taxCalculator;
    private final DiscountCalculator discountCalculator;
    private final RiskAssessor riskAssessor;
    private final ReportGenerator reportGenerator;

    public OrderProcessor(boolean verbose) {
        this.verbose = verbose;
        this.taxCalculator = new TaxCalculator();
        this.discountCalculator = new DiscountCalculator();
        this.riskAssessor = new RiskAssessor();
        this.reportGenerator = new ReportGenerator();
    }

    public String process(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, int riskThreshold) {
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            double lineTotal = order.getPrice() * order.getQuantity();

            if (applyDiscount) {
                lineTotal = discountCalculator.applyDiscount(lineTotal, order.getQuantity(), order.getPrice());
            }

            if (applyTax) {
                lineTotal = taxCalculator.applyTax(lineTotal, order.getCountry());
            }

            grandTotal += lineTotal;

            if (riskAssessor.isOrderSuspicious(order, riskThreshold)) {
                suspiciousCount++;
            }

            if (verbose) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        return reportGenerator.generateReport(orders, date, grandTotal, suspiciousCount);
    }
}