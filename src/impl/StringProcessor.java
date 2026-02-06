public class StringProcessor {
    public static String processInput(String input) {
        String processed = processCharacters(input);
        processed = replaceUnderscores(processed);
        return truncateIfNecessary(processed);
    }

    private static String processCharacters(String input) {
        StringBuilder result = new StringBuilder();
        int sum = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sum += processCharacter(c);
            if (sum % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }

    private static int processCharacter(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'z') {
            return c;
        } else if (c >= 'A' && c <= 'Z') {
            return c + 32;
        } else {
            return 1;
        }
    }

    private static String replaceUnderscores(String input) {
        return input.replace('_', '-');
    }

    private static String truncateIfNecessary(String input) {
        return input.length() > 50 ? input.substring(0, 50) : input;
    }
}