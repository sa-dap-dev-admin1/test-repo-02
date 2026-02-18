public class OrderValidator {
    private static final int DEFAULT_RISK_FLAG = 5;
    private static final double SUSPICIOUS_TOTAL_THRESHOLD = 300.0;

    private final int riskFlag;

    public OrderValidator() {
        this(DEFAULT_RISK_FLAG);
    }

    public OrderValidator(int riskFlag) {
        this.riskFlag = riskFlag;
    }

    public int getRiskFlag() {
        return riskFlag;
    }

    public static boolean isSuspicious(Order order, int riskFlag) {
        if (riskFlag > DEFAULT_RISK_FLAG) {
            return order.getTotal() > SUSPICIOUS_TOTAL_THRESHOLD || order.getId().startsWith("D");
        } else {
            return order.getQuantity() == 0;
        }
    }
}