import java.util.List;

public class OrderProcessor {
    public String process(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, int riskFlag,
                          TaxCalculator taxCalculator, DiscountCalculator discountCalculator,
                          RiskAssessor riskAssessor, OrderReporter reporter) {
        double grandTotal = 0;
        int suspiciousCount = 0;

        for (Order order : orders) {
            double lineTotal = order.calculateLineTotal();

            if (applyDiscount) {
                lineTotal = discountCalculator.applyDiscount(order, lineTotal);
            }

            if (applyTax) {
                lineTotal = taxCalculator.calculateTax(order, lineTotal);
            }

            if (riskAssessor.isOrderSuspicious(order, riskFlag)) {
                suspiciousCount++;
            }

            grandTotal += lineTotal;
            reporter.addOrderToReport(date, order, lineTotal);
        }

        return reporter.generateReport(orders, grandTotal, suspiciousCount);
    }
}