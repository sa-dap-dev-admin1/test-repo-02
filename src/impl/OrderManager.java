import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    private List<Order> orders;

    public OrderManager() {
        this.orders = new ArrayList<>();
    }

    public void add(String id, int quantity, double price, String country) {
        if (id == null || id.isEmpty() || quantity < 0 || price < 0 || country == null || country.isEmpty()) {
            throw new IllegalArgumentException("Invalid order parameters");
        }
        orders.add(new Order(id, quantity, price, country));
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }
}