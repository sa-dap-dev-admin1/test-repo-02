import java.util.*;

public class OrderProcessor {
    private List<Order> orders;
    private TaxCalculator taxCalculator;
    private DiscountCalculator discountCalculator;
    private RiskAnalyzer riskAnalyzer;
    private ReportGenerator reportGenerator;

    public OrderProcessor() {
        this.orders = new ArrayList<>();
        this.taxCalculator = new TaxCalculator();
        this.discountCalculator = new DiscountCalculator();
        this.riskAnalyzer = new RiskAnalyzer();
        this.reportGenerator = new ReportGenerator();
    }

    public void addOrder(String id, int quantity, double price, String country) {
        Order order = new Order.Builder()
                .id(id)
                .quantity(quantity)
                .price(price)
                .country(country)
                .build();
        orders.add(order);
    }

    public String processOrders(String date, boolean applyTax, boolean applyDiscount, int riskThreshold) {
        double grandTotal = 0;
        int suspiciousCount = 0;
        List<String> processedOrders = new ArrayList<>();

        for (Order order : orders) {
            double lineTotal = calculateLineTotal(order, applyTax, applyDiscount);
            grandTotal += lineTotal;

            if (riskAnalyzer.isOrderSuspicious(order, riskThreshold)) {
                suspiciousCount++;
            }

            processedOrders.add(reportGenerator.generateOrderLine(order, date, lineTotal));

            if (ConfigurationManager.isDebugMode()) {
                System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
            }
        }

        return reportGenerator.generateFinalReport(processedOrders, grandTotal, calculateRawTotal(), suspiciousCount);
    }

    private double calculateLineTotal(Order order, boolean applyTax, boolean applyDiscount) {
        double lineTotal = order.getQuantity() * order.getPrice();

        if (applyDiscount) {
            lineTotal = discountCalculator.applyDiscount(lineTotal, order.getQuantity(), order.getPrice());
        }

        if (applyTax) {
            lineTotal = taxCalculator.applyTax(lineTotal, order.getCountry());
        }

        return lineTotal;
    }

    public double calculateRawTotal() {
        return orders.stream()
                .mapToDouble(order -> order.getQuantity() * order.getPrice())
                .sum();
    }
}