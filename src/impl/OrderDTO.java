public class OrderDTO {
    private String id;
    private int quantity;
    private double price;
    private String country;
    private long timestamp;

    public OrderDTO(String id, int quantity, double price, String country, long timestamp) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getCountry() { return country; }
    public long getTimestamp() { return timestamp; }
}