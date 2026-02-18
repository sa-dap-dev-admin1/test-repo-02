import java.util.ArrayList;
import java.util.List;

public class OrderProcessorBad {
    public static void main(String[] args) {
        boolean verbose = args.length > 0;

        List<OrderModel.Order> orders = new ArrayList<>();
        orders.add(new OrderModel.Order("A1", 2, 199.99, OrderModel.Country.IN));
        orders.add(new OrderModel.Order("B2", 1, 9.5, OrderModel.Country.US));
        orders.add(new OrderModel.Order("C3", 5, 25.0, OrderModel.Country.IN));
        orders.add(new OrderModel.Order("D4", 3, 25.0, OrderModel.Country.US));

        OrderCalculator calculator = new OrderCalculator();
        List<OrderModel.ProcessedOrder> processedOrders = calculator.processOrders(orders, "2026-02-04", true, true, 7);

        OrderReporter reporter = new OrderReporter(verbose);
        String report = reporter.generateReport(processedOrders);
        System.out.println(report);

        double total = calculator.calculateRawTotal(orders);
        System.out.println("TOTAL=" + total);
    }
}