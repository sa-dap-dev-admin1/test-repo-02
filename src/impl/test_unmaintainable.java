import java.util.List;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor(args.length > 0);
        processor.addOrders();
        String report = processor.processOrders("2026-02-04", true, false, 7);
        System.out.println(report);
        processor.printTotal();
    }

    private static void addOrders() {
        OrderProcessor.add("A1", 2, 199.99, "IN");
        OrderProcessor.add("B2", 1, 9.5, "US");
        OrderProcessor.add("C3", 5, 25.0, "IN");
        OrderProcessor.add("D4", 3, 25.0, "US");
    }

    private static void printTotal() {
        double total = OrderCalculator.calculateRawTotal(OrderProcessor.getOrders());
        System.out.println("TOTAL=" + total);
    }
}