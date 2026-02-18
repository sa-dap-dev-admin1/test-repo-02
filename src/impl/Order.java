import java.math.BigDecimal;

public class Order {
    private String id;
    private int quantity;
    private BigDecimal price;
    private String country;

    public Order(String id, int quantity, BigDecimal price, String country) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
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

    public String getCountry() {
        return country;
    }
}