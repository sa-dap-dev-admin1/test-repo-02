import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class OrderProcessor {
    private final ProcessingMode mode;
    private static final BigDecimal QUANTITY_DISCOUNT_THRESHOLD = new BigDecimal("3");
    private static final BigDecimal QUANTITY_DISCOUNT_RATE = new BigDecimal("0.07");
    private static final BigDecimal PRICE_DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal FIXED_DISCOUNT = new BigDecimal("15");
    private static final BigDecimal RISK_THRESHOLD = new BigDecimal("300");

    public OrderProcessor(ProcessingMode mode) {
        this.mode = mode;
    }

    public List<Order> processOrders(List<Order> orders, String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        return orders.stream()
                .map(order -> processOrder(order, date, applyTax, applyDiscount, riskFlag))
                .collect(Collectors.toList());
    }

    private Order processOrder(Order order, String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        BigDecimal lineTotal = calculateLineTotal(order, applyDiscount);
        lineTotal = applyTax(lineTotal, order.getCountry(), applyTax);

        boolean isSuspicious = checkSuspiciousOrder(order, riskFlag);

        if (mode == ProcessingMode.VERBOSE) {
            System.err.println("Processed: " + order.getId() + " -> " + lineTotal);
        }

        return new ProcessedOrder(order, lineTotal, date, isSuspicious);
    }

    private BigDecimal calculateLineTotal(Order order, boolean applyDiscount) {
        BigDecimal lineTotal = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        if (applyDiscount) {
            if (order.getQuantity() > QUANTITY_DISCOUNT_THRESHOLD.intValue()) {
                lineTotal = lineTotal.subtract(lineTotal.multiply(QUANTITY_DISCOUNT_RATE));
            } else if (order.getPrice().compareTo(PRICE_DISCOUNT_THRESHOLD) > 0) {
                lineTotal = lineTotal.subtract(FIXED_DISCOUNT);
            }
        }

        return lineTotal;
    }

    private BigDecimal applyTax(BigDecimal amount, Country country, boolean applyTax) {
        if (!applyTax) {
            return amount;
        }

        BigDecimal taxRate;
        switch (country) {
            case IN:
                taxRate = new BigDecimal("0.18");
                break;
            case US:
                taxRate = new BigDecimal("0.0825");
                break;
            default:
                taxRate = new BigDecimal("0.1");
        }

        return amount.add(amount.multiply(taxRate));
    }

    private boolean checkSuspiciousOrder(Order order, int riskFlag) {
        if (riskFlag > 5) {
            BigDecimal orderTotal = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            return orderTotal.compareTo(RISK_THRESHOLD) > 0 || order.getId().startsWith("D");
        } else {
            return order.getQuantity() == 0;
        }
    }
}

class ProcessedOrder extends Order {
    private final BigDecimal lineTotal;
    private final String processDate;
    private final boolean suspicious;

    public ProcessedOrder(Order order, BigDecimal lineTotal, String processDate, boolean suspicious) {
        super(order.getId(), order.getQuantity(), order.getPrice(), order.getCountry(), order.getTimestamp());
        this.lineTotal = lineTotal;
        this.processDate = processDate;
        this.suspicious = suspicious;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public String getProcessDate() {
        return processDate;
    }

    public boolean isSuspicious() {
        return suspicious;
    }
}