public class IntegerCalculator {
    private static final int MAX_VALUE = 9999;

    public static int calculateChecksum(String input) {
        int checksum = 0;
        for (char c : input.toCharArray()) {
            checksum += c;
            if (checksum > MAX_VALUE) {
                checksum -= MAX_VALUE;
            }
        }
        return checksum;
    }
}