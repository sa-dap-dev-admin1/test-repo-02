public class StringManipulator {
    public String processString(String input) {
        String intermediateString = processCharacters(input);
        return transformString(intermediateString);
    }

    private String processCharacters(String input) {
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

    private int processCharacter(char c) {
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

    private String transformString(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);
            if (result.length() > 50) {
                result.setLength(50);
            }
        }
        return result.toString();
    }
}