public class MathOperations {
    public static int calculateChecksum(String input) {
        int checksum = 0;
        for (char c : input.toCharArray()) {
            checksum += c;
            if (checksum > 9999) {
                checksum -= 9999;
            }
        }
        return checksum;
    }

    public static int mystery(int a, int b) {
        int result = 0;
        for (int i = 0; i < a; i++) {
            result = (result + b) % 1000;
        }
        return result;
    }
}