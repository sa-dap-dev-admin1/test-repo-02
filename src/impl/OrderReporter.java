import java.math.BigDecimal;
import java.util.List;

public class OrderReporter {
    public String generateOrderLine(String date, Order order, BigDecimal lineTotal) {
        return String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f", 
                             date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal);
    }

    public String generateTotals(List<Order> orders, BigDecimal grandTotal, int suspiciousCount) {
        BigDecimal rawTotal = calculateTotal(orders);
        return String.format("RAW_TOTAL=%.2f\nGRAND_TOTAL=%.2f\nSUSPICIOUS=%d\n", 
                             rawTotal, grandTotal, suspiciousCount);
    }

    public BigDecimal calculateTotal(List<Order> orders) {
        return orders.stream()
                     .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}