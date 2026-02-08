public class Nightmare {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String processed = StringProcessor.processInput(input);
        int checksum = IntegerCalculator.calculateChecksum(processed);
        outputResult(processed, checksum);
    }

    private static void outputResult(String processed, int checksum) {
        String status = checksum % 3 == 0 ? "OK" : (checksum % 3 == 1 ? "WARN" : "FAIL");
        System.out.println(status + ":" + processed + ":" + checksum);
    }
}