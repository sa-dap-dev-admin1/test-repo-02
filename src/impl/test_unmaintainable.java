import java.util.function.Function;

public class Nightmare {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        StringProcessor processor = new StringProcessor();
        String processedString = processor.processInput(input);
        int checksum = MathOperations.calculateChecksum(processedString);
        outputResult(processedString, checksum);
    }

    private static void outputResult(String result, int checksum) {
        String status;
        if (checksum % 3 == 0) {
            status = "OK";
        } else if (checksum % 3 == 1) {
            status = "WARN";
        } else {
            status = "FAIL";
        }
        System.out.println(status + ":" + result + ":" + checksum);
    }
}