public class Configuration {
    private static boolean debugMode;
    private final String date;
    private final boolean applyTax;
    private final boolean applyDiscount;

    public Configuration(boolean debugMode) {
        Configuration.debugMode = debugMode;
        this.date = "2026-02-04";
        this.applyTax = true;
        this.applyDiscount = true;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public String getDate() {
        return date;
    }

    public boolean isApplyTax() {
        return applyTax;
    }

    public boolean isApplyDiscount() {
        return applyDiscount;
    }
}