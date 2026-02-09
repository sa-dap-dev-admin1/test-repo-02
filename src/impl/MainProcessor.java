public class MainProcessor {
    private StringManipulator stringManipulator;
    private ChecksumCalculator checksumCalculator;

    public MainProcessor() {
        this.stringManipulator = new StringManipulator();
        this.checksumCalculator = new ChecksumCalculator();
    }

    public void processInput(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String processedString = stringManipulator.processString(input);
        int checksum = checksumCalculator.calculateChecksum(processedString);
        printOutput(processedString, checksum);
    }

    private void printOutput(String processedString, int checksum) {
        String status;
        if (checksum % 3 == 0) {
            status = "OK";
        } else if (checksum % 3 == 1) {
            status = "WARN";
        } else {
            status = "FAIL";
        }
        System.out.println(status + ":" + processedString + ":" + checksum);
    }
}