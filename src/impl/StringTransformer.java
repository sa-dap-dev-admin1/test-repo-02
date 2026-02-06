public class StringTransformer {
    private static final int MAX_LENGTH = 50;
    private static final int HALF_LENGTH = 25;

    public String transformString(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);

            if (result.length() > MAX_LENGTH) {
                result.delete(HALF_LENGTH, result.length() - HALF_LENGTH);
            }
        }

        return result.toString();
    }

    public int calculateChecksum(String input) {
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