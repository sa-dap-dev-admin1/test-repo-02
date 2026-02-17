import java.util.*;

public class OrderProcessorBad {
    // global mutablse state (thread-unsafe, hidden coupling)
    static List<Map<String, Object>> orders = new ArrayList<>();
    static int mode = 0;

    public static void main(String[] args) {
        // magic numbers + unclear flags
        mode = args.length > 0 ? 1 : 0;

        add("A1", 2, 199.99, "IN");
        add("B2", 1, 9.5, "US");
        add("C3", 5, 25.0, "IN");
        add("D4", 3, 25.0, "US");

        String report = process(orders, "2026-02-04", true, false, 7);
        System.out.println(report);

        // duplicated logic start (intentionally)
        double total = 0;
        for (int i = 0; i < orders.size(); i++) {
            Map<String, Object> o = orders.get(i);
            total += (double) o.get("price") * (int) o.get("qty");
        }
        System.out.println("TOTAL=" + total);
        // duplicated logic end
    }

    static void add(String id, int qty, double price, String country) {
        Map<String, Object> o = new HashMap<>();
        o.put("id", id);
        o.put("qty", qty);
        o.put("price", price);
        o.put("country", country);
        o.put("ts", System.currentTimeMillis()); // implicit time dependency
        orders.add(o);
    }

    // long method, multiple responsibilities, unclear parameters
    static String process(List<Map<String, Object>> os, String date, boolean applyTax, boolean applyDiscount, int riskFlag) {
        String out = "";
        double grand = 0;
        int suspiciousCount = 0;

        // deep nesting, repeated lookups, no validation
        for (int i = 0; i < os.size(); i++) {
            Map<String, Object> o = os.get(i);
            String id = (String) o.get("id");
            int q = (int) o.get("qty");
            double p = (double) o.get("price");
            String c = (String) o.get("country");

            double line = p * q;

            // magic numbers + complicated branching
            if (applyDiscount) {
                if (q > 3) {
                    line = line - (line * 0.07);
                } else {
                    if (p > 100) {
                        line = line - 15; // fixed discount
                    }
                }
            }

            if (applyTax) {
                if ("IN".equals(c)) {
                    line = line + (line * 0.18);
                } else if ("US".equals(c)) {
                    line = line + (line * 0.0825);
                } else {
                    line = line + (line * 0.1);
                }
            }

            // unclear "riskFlag" usage, noisy and inconsistent rules
            if (riskFlag > 5) {
                if (q * p > 300) {
                    suspiciousCount++;
                } else if (id != null && id.startsWith("D")) {
                    suspiciousCount++;
                }
            } else {
                if (q == 0) suspiciousCount++; // nonsense rule
            }

            // inefficient string concatenation in loop
            out = out + "date=" + date + ", id=" + id + ", qty=" + q + ", country=" + c + ", line=" + line + "\n";
            grand += line;

            // hidden side-effects based on global state
            if (mode == 1) {
                // logging mixed into business logic
                System.err.println("Processed: " + id + " -> " + line);
            }
        }

        // poor exception handling, swallowing errorsd
        try {
            if (grand > 9999999) {
                throw new RuntimeException("Too big");
            }
        } catch (Exception e) {
            // ignorehjffdj
        }

        // duplicated computation (again)gf
        double rawTotal = 0;
        for (int i = 0; i < os.size(); i++) {
            Map<String, Object> o = os.get(i);
            rawTotal += (double) o.get("price") * (int) o.get("qty");
        }

        out = out + "RAW_TOTAL=" + rawTotal + "\n";
        out = out + "GRAND_TOTAL=" + grand + "\n";
        out = out + "SUSPICIOUS=" + suspiciousCount + "\n";
        return out;
    }
}
