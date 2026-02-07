public class StringProcessor {
    public static String processInput(String input) {
        StringBuilder processedString = new StringBuilder();
        int z = 0;
        for (int x = 0; x < input.length(); x++) {
            char c = input.charAt(x);
            z += processCharacter(c);
            if (z % 2 == 0) {
                processedString.append(c);
            } else {
                processedString.insert(0, c);
            }
        }
        return transformString(processedString.toString());
    }

    private static int processCharacter(char c) {
        if (Character.isDigit(c)) {
            return c - '0';
        } else if (Character.isLowerCase(c)) {
            return c;
        } else if (Character.isUpperCase(c)) {
            return c + 32;
        } else {
            return 1;
        }
    }

    private static String transformString(String input) {
        StringBuilder result = new StringBuilder();
        for (char d : input.toCharArray()) {
            result.append(d == '_' ? '-' : d);
            if (result.length() > 50) {
                result.setLength(50);
                break;
            }
        }
        return result.toString();
    }
}