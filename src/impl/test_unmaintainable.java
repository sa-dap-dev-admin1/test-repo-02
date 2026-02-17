import java.util.List;

public class OrderProcessorBad {
    public static void main(String[] args) {
        ProcessingMode mode = args.length > 0 ? ProcessingMode.VERBOSE : ProcessingMode.NORMAL;
        
        OrderData orderData = new OrderData();
        orderData.addSampleOrders();

        OrderProcessor processor = new OrderProcessor(mode);
        List<Order> processedOrders = processor.processOrders(orderData.getOrders(), "2026-02-04", true, false, 7);

        ReportGenerator reportGenerator = new ReportGenerator();
        String report = reportGenerator.generateReport(processedOrders);
        System.out.println(report);

        double total = orderData.calculateRawTotal();
        System.out.println("TOTAL=" + total);
    }
}