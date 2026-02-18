import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private List<Order> orders = new ArrayList<>();

    public void add(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public double calculateTotalOrderValue() {
        return orders.stream().mapToDouble(Order::calculateLineTotal).sum();
    }
}