public class RiskAssessor {
    private static final double HIGH_VALUE_THRESHOLD = 300;

    public boolean isOrderSuspicious(Order order, int riskFlag) {
        if (riskFlag > 5) {
            return isHighValueOrder(order) || isDSeriesOrder(order);
        } else {
            return isZeroQuantityOrder(order);
        }
    }

    private boolean isHighValueOrder(Order order) {
        return order.getQuantity() * order.getPrice() > HIGH_VALUE_THRESHOLD;
    }

    private boolean isDSeriesOrder(Order order) {
        return order.getId() != null && order.getId().startsWith("D");
    }

    private boolean isZeroQuantityOrder(Order order) {
        return order.getQuantity() == 0;
    }
}