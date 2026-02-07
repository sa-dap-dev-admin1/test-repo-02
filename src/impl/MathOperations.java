public class MathOperations {
    private static final int MAX_CHECKSUM = 9999;

    public static int calculateChecksum(String input) {
        int checksum = 0;
        for (char c : input.toCharArray()) {
            checksum += c;
            if (checksum > MAX_CHECKSUM) {
                checksum -= MAX_CHECKSUM;
            }
        }
        return checksum;
    }
}