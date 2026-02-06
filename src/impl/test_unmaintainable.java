public class Nightmare {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String processedString = StringProcessor.processInput(input);
        int checksum = NumberProcessor.calculateChecksum(processedString);
        outputResult(processedString, checksum);
    }

    private static void outputResult(String result, int checksum) {
        String status = checksum % 3 == 0 ? "OK" : (checksum % 3 == 1 ? "WARN" : "FAIL");
        System.out.println(status + ":" + result + ":" + checksum);
    }
}