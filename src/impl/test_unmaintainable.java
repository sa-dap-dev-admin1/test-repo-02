import java.util.*;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        orderService.setMode(args.length > 0);

        orderService.addOrder("A1", 2, 199.99, "IN");
        orderService.addOrder("B2", 1, 9.5, "US");
        orderService.addOrder("C3", 5, 25.0, "IN");
        orderService.addOrder("D4", 3, 25.0, "US");

        String report = orderService.processOrders("2026-02-04", true, false, 7);
        System.out.println(report);

        double total = orderService.calculateTotal();
        System.out.println("TOTAL=" + total);
    }
}