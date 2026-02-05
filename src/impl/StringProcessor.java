package patterns.java;

public class StringProcessor {

    public String process(String input) {
        if (!isValidInput(input)) {
            return "STATUS:INVALID";
        }

        String normalized = normalize(input);
        int checksum = calculateChecksum(normalized);

        return buildResult(normalized, checksum);
    }

    private boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }

    private String normalize(String input) {
        StringBuilder normalized = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                normalized.append(c);
            } else {
                normalized.append('_');
            }
        }
        return normalized.toString();
    }

    private int calculateChecksum(String normalized) {
        int checksum = 0;
        for (char c : normalized.toCharArray()) {
            checksum = (checksum + c) % 100000;
        }
        return checksum;
    }

    private String buildResult(String normalized, int checksum) {
        return String.format("STATUS:OK;VALUE:%s;CHECKSUM:%d", normalized, checksum);
    }
}