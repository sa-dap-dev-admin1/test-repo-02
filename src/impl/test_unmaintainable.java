import java.util.List;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        OrderRepository repository = new OrderRepository();
        TaxCalculator taxCalculator = new TaxCalculatorFactory().createTaxCalculator();
        DiscountCalculator discountCalculator = new DiscountCalculatorFactory().createDiscountCalculator();
        RiskAssessor riskAssessor = new RiskAssessor();
        OrderReporter reporter = new OrderReporter();

        repository.add(new Order("A1", 2, 199.99, "IN"));
        repository.add(new Order("B2", 1, 9.5, "US"));
        repository.add(new Order("C3", 5, 25.0, "IN"));
        repository.add(new Order("D4", 3, 25.0, "US"));

        String report = processor.process(repository.getOrders(), "2026-02-04", true, true, 7,
                taxCalculator, discountCalculator, riskAssessor, reporter);
        System.out.println(report);

        double total = repository.calculateTotalOrderValue();
        System.out.println("TOTAL=" + total);
    }
}