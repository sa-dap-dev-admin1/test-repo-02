public class Order {
    private final String id;
    private final int quantity;
    private final double price;
    private final String country;
    private final long timestamp;

    public Order(String id, int quantity, double price, String country) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
        this.timestamp = System.currentTimeMillis();
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

    public long getTimestamp() {
        return timestamp;
    }
}