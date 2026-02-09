public class ChecksumCalculator {
    public static int calculate(String input) {
        int checksum = 0;
        
        for (char c : input.toCharArray()) {
            checksum += c;
            if (checksum > 9999) {
                checksum -= 9999;
            }
        }
        
        return checksum;
    }
}