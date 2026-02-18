public class ProcessedOrder {
    private Order order;
    private double total;

    public ProcessedOrder(Order order, double total) {
        this.order = order;
        this.total = total;
    }

    public Order getOrder() {
        return order;
    }

    public double getTotal() {
        return total;
    }
}