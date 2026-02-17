public interface RiskAssessor {
    boolean assessRisk(Order order, int riskFlag);
}

class DefaultRiskAssessor implements RiskAssessor {
    @Override
    public boolean assessRisk(Order order, int riskFlag) {
        if (riskFlag > 5) {
            return (order.getQuantity() * order.getPrice() > 300) || 
                   (order.getId() != null && order.getId().startsWith("D"));
        } else {
            return order.getQuantity() == 0;
        }
    }
}