public class LoggingUtil {
    public static void log(String message) {
        System.err.println(message);
    }

    public static void logError(String errorMessage) {
        System.err.println("ERROR: " + errorMessage);
    }
}