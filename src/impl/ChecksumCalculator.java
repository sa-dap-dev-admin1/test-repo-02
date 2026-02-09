public class ChecksumCalculator {
    private static final int CHECKSUM_THRESHOLD = 9999;

    public static int calculateChecksum(String input) {
        int checksum = 0;
        for (char c : input.toCharArray()) {
            checksum += c;
            if (checksum > CHECKSUM_THRESHOLD) {
                checksum -= CHECKSUM_THRESHOLD;
            }
        }
        return checksum;
    }
}