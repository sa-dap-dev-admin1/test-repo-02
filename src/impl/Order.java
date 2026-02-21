import java.util.HashMap;
import java.util.Map;

public class Order {
    private String id;
    private int quantity;
    private double price;
    private String country;
    private long timestamp;

    private Order(Builder builder) {
        this.id = builder.id;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.country = builder.country;
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

    public static class Builder {
        private String id;
        private int quantity;
        private double price;
        private String country;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("qty", quantity);
        map.put("price", price);
        map.put("country", country);
        map.put("ts", timestamp);
        return map;
    }
}