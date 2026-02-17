import java.util.List;

public class Main {
    public static void main(String[] args) {
        boolean isVerboseMode = args.length > 0;

        OrderManager orderManager = new OrderManager();
        orderManager.add("A1", 2, 199.99, "IN");
        orderManager.add("B2", 1, 9.5, "US");
        orderManager.add("C3", 5, 25.0, "IN");
        orderManager.add("D4", 3, 25.0, "US");

        OrderProcessor orderProcessor = new OrderProcessor(isVerboseMode);
        List<Order> processedOrders = orderProcessor.processOrders(orderManager.getOrders(), "2026-02-04", true, true, 7);

        OrderReporter orderReporter = new OrderReporter();
        String report = orderReporter.generateReport(processedOrders);
        System.out.println(report);

        double total = orderReporter.calculateRawTotal(processedOrders);
        System.out.println("TOTAL=" + total);
    }
}