public class RiskAssessor {
    public boolean isOrderSuspicious(Order order, int riskFlag) {
        if (riskFlag > 5) {
            return order.calculateLineTotal() > 300 || order.getId().startsWith("D");
        } else {
            return order.getQuantity() == 0;
        }
    }
}