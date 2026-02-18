import java.math.BigDecimal;
import java.util.List;

public class OrderReportGenerator {
    public String generateReport(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        StringBuilder report = new StringBuilder();
        BigDecimal grandTotal = BigDecimal.ZERO;
        int suspiciousCount = 0;

        for (Order order : orders) {
            BigDecimal lineTotal = order.getTotal();
            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    date, order.getId(), order.getQuantity(), order.getCountry(), lineTotal));
            grandTotal = grandTotal.add(lineTotal);

            if (isOrderSuspicious(order, riskFlag)) {
                suspiciousCount++;
            }
        }

        report.append(String.format("RAW_TOTAL=%.2f%n", calculateTotal(orders)));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));

        return report.toString();
    }

    public BigDecimal calculateTotal(List<Order> orders) {
        return orders.stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isOrderSuspicious(Order order, int riskFlag) {
        if (riskFlag > 5) {
            BigDecimal orderValue = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            return orderValue.compareTo(BigDecimal.valueOf(300)) > 0 || order.getId().startsWith("D");
        } else {
            return order.getQuantity() == 0;
        }
    }
}