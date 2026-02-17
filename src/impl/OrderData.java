import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderData {
    private List<Order> orders;

    public OrderData() {
        this.orders = new ArrayList<>();
    }

    public void addSampleOrders() {
        addOrder("A1", 2, new BigDecimal("199.99"), Country.IN);
        addOrder("B2", 1, new BigDecimal("9.5"), Country.US);
        addOrder("C3", 5, new BigDecimal("25.0"), Country.IN);
        addOrder("D4", 3, new BigDecimal("25.0"), Country.US);
    }

    public void addOrder(String id, int quantity, BigDecimal price, Country country) {
        Order order = new Order(id, quantity, price, country, Instant.now());
        orders.add(order);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public BigDecimal calculateRawTotal() {
        return orders.stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

class Order {
    private final String id;
    private final int quantity;
    private final BigDecimal price;
    private final Country country;
    private final Instant timestamp;

    public Order(String id, int quantity, BigDecimal price, Country country, Instant timestamp) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (country == null) {
            throw new IllegalArgumentException("Country cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Country getCountry() {
        return country;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

enum Country {
    IN, US, OTHER
}

enum ProcessingMode {
    NORMAL, VERBOSE
}