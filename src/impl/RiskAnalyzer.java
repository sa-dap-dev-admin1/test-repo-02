public class RiskAnalyzer {
    private static final double HIGH_VALUE_THRESHOLD = 300;

    public boolean isOrderSuspicious(Order order, int riskThreshold) {
        if (riskThreshold > 5) {
            double orderValue = order.getQuantity() * order.getPrice();
            if (orderValue > HIGH_VALUE_THRESHOLD) {
                return true;
            }
            if (order.getId() != null && order.getId().startsWith("D")) {
                return true;
            }
        } else {
            if (order.getQuantity() == 0) {
                return true;
            }
        }
        return false;
    }
}