public class StringProcessor {
    public static String processString(String input) {
        String processed = processCharacters(input);
        processed = replaceUnderscores(processed);
        return truncateIfNecessary(processed);
    }

    private static String processCharacters(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            z = updateZ(z, c);
            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }

    private static int updateZ(int z, char c) {
        if (c >= '0' && c <= '9') {
            return z + (c - '0');
        } else if (c >= 'a' && c <= 'z') {
            return z + c;
        } else if (c >= 'A' && c <= 'Z') {
            return z + (c + 32);
        } else {
            return z + 1;
        }
    }

    private static String replaceUnderscores(String input) {
        return input.replace('_', '-');
    }

    private static String truncateIfNecessary(String input) {
        return input.length() > 50 ? input.substring(0, 50) : input;
    }

    public static String helper(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.length() > 0 && result.charAt(0) == 'x' ? result.substring(1) : result.toString();
    }
}