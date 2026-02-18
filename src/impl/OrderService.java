import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private List<Order> orders = new ArrayList<>();

    public void addOrder(String id, int quantity, double price, Country country) {
        orders.add(new Order(id, quantity, BigDecimal.valueOf(price), country));
    }

    public List<Order> processOrders(boolean verbose) {
        List<Order> processedOrders = new ArrayList<>();
        for (Order order : orders) {
            Order processedOrder = processOrder(order);
            processedOrders.add(processedOrder);
            if (verbose) {
                System.err.println("Processed: " + processedOrder.getId() + " -> " + processedOrder.getTotal());
            }
        }
        return processedOrders;
    }

    private Order processOrder(Order order) {
        BigDecimal total = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        total = applyDiscount(total, order.getQuantity(), order.getPrice());
        total = applyTax(total, order.getCountry());
        return new Order(order.getId(), order.getQuantity(), order.getPrice(), order.getCountry(), total);
    }

    private BigDecimal applyDiscount(BigDecimal total, int quantity, BigDecimal price) {
        if (quantity > 3) {
            return total.multiply(BigDecimal.valueOf(0.93)); // 7% discount
        } else if (price.compareTo(BigDecimal.valueOf(100)) > 0) {
            return total.subtract(BigDecimal.valueOf(15)); // $15 fixed discount
        }
        return total;
    }

    private BigDecimal applyTax(BigDecimal total, Country country) {
        switch (country) {
            case IN:
                return total.multiply(BigDecimal.valueOf(1.18)); // 18% tax
            case US:
                return total.multiply(BigDecimal.valueOf(1.0825)); // 8.25% tax
            default:
                return total.multiply(BigDecimal.valueOf(1.1)); // 10% tax
        }
    }
}