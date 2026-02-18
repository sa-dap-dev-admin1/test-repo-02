import java.util.List;

public class ReportGenerator {
    public String generateReport(List<Order> orders, String date, double grandTotal, int suspiciousCount) {
        StringBuilder report = new StringBuilder();
        for (Order order : orders) {
            report.append(String.format("date=%s, id=%s, qty=%d, country=%s, line=%.2f%n",
                    date, order.getId(), order.getQuantity(), order.getCountry(),
                    order.getPrice() * order.getQuantity()));
        }
        report.append(String.format("RAW_TOTAL=%.2f%n", calculateRawTotal(orders)));
        report.append(String.format("GRAND_TOTAL=%.2f%n", grandTotal));
        report.append(String.format("SUSPICIOUS=%d%n", suspiciousCount));
        return report.toString();
    }

    private double calculateRawTotal(List<Order> orders) {
        return orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}