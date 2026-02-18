import java.math.BigDecimal;

public class RiskAnalyzer {
    private ConfigurationManager config;

    public RiskAnalyzer(ConfigurationManager config) {
        this.config = config;
    }

    public boolean isOrderSuspicious(Order order) {
        BigDecimal orderTotal = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        return orderTotal.compareTo(BigDecimal.valueOf(300)) > 0 || 
               (order.getId() != null && order.getId().startsWith("D"));
    }
}