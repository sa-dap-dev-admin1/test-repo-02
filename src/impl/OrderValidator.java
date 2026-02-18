public class OrderValidator {
    private static final double SUSPICIOUS_TOTAL_THRESHOLD = 300;
    private static final String SUSPICIOUS_ID_PREFIX = "D";
    private static final int SUSPICIOUS_RISK_THRESHOLD = 5;

    public static boolean isSuspicious(Order order, int riskFlag) {
        if (riskFlag > SUSPICIOUS_RISK_THRESHOLD) {
            double orderTotal = order.getPrice() * order.getQuantity();
            return orderTotal > SUSPICIOUS_TOTAL_THRESHOLD ||
                   (order.getId() != null && order.getId().startsWith(SUSPICIOUS_ID_PREFIX));
        } else {
            return order.getQuantity() == 0;
        }
    }
}