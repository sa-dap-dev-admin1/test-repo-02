import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private static OrderRepository instance = new OrderRepository();
    private List<Order> orders = new ArrayList<>();

    private OrderRepository() {}

    public static OrderRepository getInstance() {
        return instance;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }
}