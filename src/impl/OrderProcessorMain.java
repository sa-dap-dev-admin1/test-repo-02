import java.util.ArrayList;
import java.util.List;

public class OrderProcessorMain {
    public static void main(String[] args) {
        boolean verbose = args.length > 0;
        
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("A1", 2, 199.99, Country.IN));
        orders.add(new Order("B2", 1, 9.5, Country.US));
        orders.add(new Order("C3", 5, 25.0, Country.IN));
        orders.add(new Order("D4", 3, 25.0, Country.US));

        OrderCalculator calculator = new OrderCalculator();
        List<ProcessedOrder> processedOrders = calculator.processOrders(orders, "2026-02-04", true, true, 7);

        OrderReporter reporter = new OrderReporter(verbose);
        String report = reporter.generateReport(processedOrders);
        System.out.println(report);

        double total = calculator.calculateRawTotal(orders);
        System.out.println("TOTAL=" + total);
    }
}