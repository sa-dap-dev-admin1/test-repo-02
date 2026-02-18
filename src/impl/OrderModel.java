import java.time.LocalDateTime;

public class OrderModel {
    public enum Country {
        IN, US, OTHER
    }

    public static class Order {
        private final String id;
        private final int quantity;
        private final double price;
        private final Country country;
        private final LocalDateTime timestamp;

        public Order(String id, int quantity, double price, Country country) {
            this.id = id;
            this.quantity = quantity;
            this.price = price;
            this.country = country;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public String getId() { return id; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public Country getCountry() { return country; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class ProcessedOrder {
        private final Order order;
        private final double lineTotal;
        private final boolean suspicious;

        public ProcessedOrder(Order order, double lineTotal, boolean suspicious) {
            this.order = order;
            this.lineTotal = lineTotal;
            this.suspicious = suspicious;
        }

        // Getters
        public Order getOrder() { return order; }
        public double getLineTotal() { return lineTotal; }
        public boolean isSuspicious() { return suspicious; }
    }
}