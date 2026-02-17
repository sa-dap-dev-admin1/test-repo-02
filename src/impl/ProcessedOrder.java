public class ProcessedOrder extends Order {
    private final String processDate;
    private final double lineTotal;
    private final boolean isSuspicious;

    public ProcessedOrder(Order order, String processDate, double lineTotal, boolean isSuspicious) {
        super(order.getId(), order.getQuantity(), order.getPrice(), order.getCountry());
        this.processDate = processDate;
        this.lineTotal = lineTotal;
        this.isSuspicious = isSuspicious;
    }

    public String getProcessDate() {
        return processDate;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public boolean isSuspicious() {
        return isSuspicious;
    }
}