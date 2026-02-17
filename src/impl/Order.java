import java.time.Instant;

public class Order {
    private final String id;
    private final int quantity;
    private final double price;
    private final String country;
    private final Instant timestamp;

    public Order(String id, int quantity, double price, String country) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getCountry() {
        return country;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}