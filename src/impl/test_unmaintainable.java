import java.util.*;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderCalculator calculator = new OrderCalculator();
        OrderValidator validator = new OrderValidator();
        Configuration config = new Configuration(args.length > 0);

        addOrders();

        String report = processOrders(calculator, validator, config);
        System.out.println(report);

        printTotalAmount();
    }

    private static void addOrders() {
        OrderCalculator.addOrder("A1", 2, 199.99, "IN");
        OrderCalculator.addOrder("B2", 1, 9.5, "US");
        OrderCalculator.addOrder("C3", 5, 25.0, "IN");
        OrderCalculator.addOrder("D4", 3, 25.0, "US");
    }

    private static String processOrders(OrderCalculator calculator, OrderValidator validator, Configuration config) {
        return calculator.process(config.getDate(), config.isApplyTax(), config.isApplyDiscount(), validator.getRiskFlag());
    }

    private static void printTotalAmount() {
        double total = OrderCalculator.calculateRawTotal();
        System.out.println("TOTAL=" + total);
    }
}