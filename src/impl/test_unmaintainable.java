import java.util.*;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor(args.length > 0);
        processor.addOrder("A1", 2, 199.99, "IN");
        processor.addOrder("B2", 1, 9.5, "US");
        processor.addOrder("C3", 5, 25.0, "IN");
        processor.addOrder("D4", 3, 25.0, "US");

        String report = processor.processOrders("2026-02-04", true, false, 7);
        System.out.println(report);

        double total = processor.calculateTotalAmount();
        System.out.println("TOTAL=" + total);
    }
}