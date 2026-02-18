public class Order {
    private String id;
    private int quantity;
    private double price;
    private String country;
    private long timestamp;

    public Order(String id, int quantity, double price, String country) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
        this.timestamp = System.currentTimeMillis();
    }

    public double calculateLineTotal() {
        return price * quantity;
    }

    // Getters
    public String getId() { return id; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getCountry() { return country; }
    public long getTimestamp() { return timestamp; }
}