package patterns.java;

public class StringProcessor {
    private StringNormalizer normalizer;
    private ChecksumCalculator checksumCalculator;

    public StringProcessor(StringNormalizer normalizer, ChecksumCalculator checksumCalculator) {
        this.normalizer = normalizer;
        this.checksumCalculator = checksumCalculator;
    }

    public String processString(String input) {
        if (!isValidInput(input)) {
            return "STATUS:INVALID";
        }

        String normalized = normalizer.normalize(input);
        int checksum = checksumCalculator.calculateChecksum(normalized);

        return buildResult(normalized, checksum);
    }

    private boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty();
    }

    private String buildResult(String normalized, int checksum) {
        return String.format("STATUS:OK;VALUE:%s;CHECKSUM:%d", normalized, checksum);
    }

    public interface StringNormalizer {
        String normalize(String input);
    }

    public interface ChecksumCalculator {
        int calculateChecksum(String input);
    }

    public static class DefaultStringNormalizer implements StringNormalizer {
        @Override
        public String normalize(String input) {
            StringBuilder normalized = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    normalized.append(Character.toLowerCase(c));
                } else if (Character.isLetterOrDigit(c)) {
                    normalized.append(c);
                } else {
                    normalized.append('_');
                }
            }
            return normalized.toString();
        }
    }

    public static class SimpleChecksumCalculator implements ChecksumCalculator {
        @Override
        public int calculateChecksum(String input) {
            int checksum = 0;
            for (char c : input.toCharArray()) {
                checksum = (checksum + c) % 100000;
            }
            return checksum;
        }
    }
}