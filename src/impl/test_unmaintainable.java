import java.util.List;

public class OrderProcessorBad {
    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor(args.length > 0);
        OrderRepository repository = new OrderRepository();

        repository.add("A1", 2, 199.99, "IN");
        repository.add("B2", 1, 9.5, "US");
        repository.add("C3", 5, 25.0, "IN");
        repository.add("D4", 3, 25.0, "US");

        String report = processor.process(repository.getOrders(), "2026-02-04", true, true, 7);
        System.out.println(report);

        double total = repository.calculateTotal();
        System.out.println("TOTAL=" + total);
    }
}