import java.math.BigDecimal;

public class Order {
    private final String id;
    private final int quantity;
    private final BigDecimal price;
    private final Country country;
    private final BigDecimal total;

    public Order(String id, int quantity, BigDecimal price, Country country) {
        this(id, quantity, price, country, null);
    }

    public Order(String id, int quantity, BigDecimal price, Country country, BigDecimal total) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.country = country;
        this.total = total;
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

    public BigDecimal getTotal() {
        return total;
    }
}