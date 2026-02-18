import java.util.ArrayList;
import java.util.List;

public class OrderProcessorMain {
    public static void main(String[] args) {
        boolean isVerboseMode = args.length > 0;
        List<Order> orders = new ArrayList<>();

        addOrder(orders, "A1", 2, 199.99, "IN");
        addOrder(orders, "B2", 1, 9.5, "US");
        addOrder(orders, "C3", 5, 25.0, "IN");
        addOrder(orders, "D4", 3, 25.0, "US");

        OrderCalculator calculator = new OrderCalculator();
        OrderReport report = calculator.processOrders(orders, "2026-02-04", true, true, 7);

        System.out.println(report.generateReport(isVerboseMode));
        System.out.println("TOTAL=" + report.getRawTotal());
    }

    private static void addOrder(List<Order> orders, String id, int quantity, double price, String country) {
        orders.add(new Order(id, quantity, price, country));
    }
}