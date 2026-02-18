import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    public void add(String id, int quantity, double price, String country) {
        orders.add(new Order(id, quantity, price, country));
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public double calculateTotal() {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}