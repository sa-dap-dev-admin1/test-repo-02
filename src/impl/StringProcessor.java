public class StringProcessor {
    private static final int MAX_LENGTH = 50;
    private static final int TRUNCATE_LENGTH = 25;

    public String processInput(String input) {
        String processed = processCharacters(input);
        processed = replaceUnderscores(processed);
        return truncateIfNecessary(processed);
    }

    private String processCharacters(String input) {
        StringBuilder result = new StringBuilder();
        int sum = 0;
        for (char c : input.toCharArray()) {
            sum += processCharacter(c);
            if (sum % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }

    private int processCharacter(char c) {
        if (Character.isDigit(c)) {
            return Character.getNumericValue(c);
        } else if (Character.isLowerCase(c)) {
            return c;
        } else if (Character.isUpperCase(c)) {
            return c + 32;
        } else {
            return 1;
        }
    }

    private String replaceUnderscores(String input) {
        return input.replace('_', '-');
    }

    private String truncateIfNecessary(String input) {
        if (input.length() > MAX_LENGTH) {
            return input.substring(0, TRUNCATE_LENGTH) + input.substring(TRUNCATE_LENGTH);
        }
        return input;
    }
}