public class OrderValidator {
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 300;

    public boolean isSuspicious(ProcessedOrder processedOrder, int riskThreshold) {
        Order order = processedOrder.getOrder();
        if (riskThreshold > 5) {
            return isHighRiskSuspicious(order);
        } else {
            return isLowRiskSuspicious(order);
        }
    }

    private boolean isHighRiskSuspicious(Order order) {
        return order.getQuantity() * order.getPrice() > SUSPICIOUS_AMOUNT_THRESHOLD ||
                (order.getId() != null && order.getId().startsWith("D"));
    }

    private boolean isLowRiskSuspicious(Order order) {
        return order.getQuantity() == 0;
    }
}