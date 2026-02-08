public class ChecksumCalculator {
    public int calculate(String input) {
        int checksum = 0;
        for (int i = 0; i < input.length(); i++) {
            checksum += input.charAt(i);
            if (checksum > 9999) {
                checksum -= 9999;
            }
        }
        return checksum;
    }
}