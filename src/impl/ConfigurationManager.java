public class ConfigurationManager {
    private static boolean debugMode = false;

    public static void initialize(String[] args) {
        debugMode = args.length > 0;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
}