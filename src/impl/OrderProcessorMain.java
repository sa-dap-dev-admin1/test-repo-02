import java.util.List;

public class OrderProcessorMain {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        OrderReportGenerator reportGenerator = new OrderReportGenerator();

        // Add sample orders
        orderService.addOrder("A1", 2, 199.99, Country.IN);
        orderService.addOrder("B2", 1, 9.5, Country.US);
        orderService.addOrder("C3", 5, 25.0, Country.IN);
        orderService.addOrder("D4", 3, 25.0, Country.US);

        // Process orders
        List<Order> processedOrders = orderService.processOrders(args.length > 0);

        // Generate and print report
        String report = reportGenerator.generateReport(processedOrders, "2026-02-04", true, true, 7);
        System.out.println(report);

        // Print total
        System.out.println("TOTAL=" + reportGenerator.calculateTotal(processedOrders));
    }
}