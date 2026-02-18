public class OrderValidator {
    public boolean isSuspicious(OrderDTO order, int riskFlag) {
        if (riskFlag > OrderConstants.RISK_THRESHOLD) {
            return isHighRiskSuspicious(order);
        } else {
            return isLowRiskSuspicious(order);
        }
    }

    private boolean isHighRiskSuspicious(OrderDTO order) {
        return order.getQuantity() * order.getPrice() > OrderConstants.HIGH_RISK_TOTAL_THRESHOLD
                || (order.getId() != null && order.getId().startsWith("D"));
    }

    private boolean isLowRiskSuspicious(OrderDTO order) {
        return order.getQuantity() == 0;
    }
}