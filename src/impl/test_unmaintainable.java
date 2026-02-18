import java.util.List;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor(args.length > 0);
        ConfigurationManager config = new ConfigurationManager();
        OrderRepository repository = new OrderRepository();
        RiskAnalyzer riskAnalyzer = new RiskAnalyzer(config);
        OrderReporter reporter = new OrderReporter();

        processor.add("A1", 2, 199.99, "IN");
        processor.add("B2", 1, 9.5, "US");
        processor.add("C3", 5, 25.0, "IN");
        processor.add("D4", 3, 25.0, "US");

        String report = processor.process(repository.getOrders(), "2026-02-04", true, true, riskAnalyzer, reporter);
        System.out.println(report);

        System.out.println("TOTAL=" + reporter.calculateTotal(repository.getOrders()));
    }
}