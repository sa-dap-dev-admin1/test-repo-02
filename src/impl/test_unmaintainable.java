import java.util.List;

public class Nightmare {
    public static void main(String[] args) {
        String input = processInput(args);
        StringProcessor processor = new StringProcessor();
        String processedString = processor.processString(input);
        int checksum = MathOperations.calculateChecksum(processedString);
        printResult(processedString, checksum);
    }

    private static String processInput(String[] args) {
        return args != null && args.length > 0 ? args[0] : "";
    }

    private static void printResult(String result, int checksum) {
        String status = checksum % 3 == 0 ? "OK" : (checksum % 3 == 1 ? "WARN" : "FAIL");
        System.out.println(status + ":" + result + ":" + checksum);
    }
}