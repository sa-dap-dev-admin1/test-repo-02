import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private final boolean isVerboseMode;
    private final DiscountStrategy discountStrategy;
    private final TaxStrategy taxStrategy;
    private final RiskAssessor riskAssessor;

    public OrderProcessor(boolean isVerboseMode) {
        this.isVerboseMode = isVerboseMode;
        this.discountStrategy = new DefaultDiscountStrategy();
        this.taxStrategy = new DefaultTaxStrategy();
        this.riskAssessor = new DefaultRiskAssessor();
    }

    public List<Order> processOrders(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        List<Order> processedOrders = new ArrayList<>();
        for (Order order : orders) {
            double lineTotal = calculateLineTotal(order, applyTax, applyDiscount);
            boolean isSuspicious = riskAssessor.assessRisk(order, riskFlag);
            ProcessedOrder processedOrder = new ProcessedOrder(order, date, lineTotal, isSuspicious);
            processedOrders.add(processedOrder);

            if (isVerboseMode) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }
        return processedOrders;
    }

    private double calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double lineTotal = order.getPrice() * order.getQuantity();
        if (applyDiscount) {
            lineTotal = discountStrategy.applyDiscount(lineTotal, order.getQuantity(), order.getPrice());
        }
        if (applyTax) {
            lineTotal = taxStrategy.applyTax(lineTotal, order.getCountry());
        }
        return lineTotal;
    }
}