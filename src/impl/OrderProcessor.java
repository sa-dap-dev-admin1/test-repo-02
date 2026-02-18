import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderProcessor {
    private boolean verbose;
    private DiscountStrategy discountStrategy;
    private TaxStrategy taxStrategy;

    public OrderProcessor(boolean verbose) {
        this.verbose = verbose;
        this.discountStrategy = new DefaultDiscountStrategy();
        this.taxStrategy = new DefaultTaxStrategy();
    }

    public void add(String id, int qty, double price, String country) {
        OrderRepository.getInstance().addOrder(new Order(id, qty, BigDecimal.valueOf(price), country));
    }

    public String process(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, 
                          RiskAnalyzer riskAnalyzer, OrderReporter reporter) {
        StringBuilder out = new StringBuilder();
        BigDecimal grandTotal = BigDecimal.ZERO;
        int suspiciousCount = 0;

        for (Order order : orders) {
            BigDecimal lineTotal = calculateLineTotal(order, applyTax, applyDiscount);
            grandTotal = grandTotal.add(lineTotal);

            out.append(reporter.generateOrderLine(date, order, lineTotal)).append("\n");

            if (riskAnalyzer.isOrderSuspicious(order)) {
                suspiciousCount++;
            }

            if (verbose) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        out.append(reporter.generateTotals(orders, grandTotal, suspiciousCount));

        return out.toString();
    }

    private BigDecimal calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        BigDecimal lineTotal = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        if (applyDiscount) {
            lineTotal = discountStrategy.applyDiscount(order, lineTotal);
        }

        if (applyTax) {
            lineTotal = taxStrategy.applyTax(order, lineTotal);
        }

        return lineTotal.setScale(2, RoundingMode.HALF_UP);
    }
}