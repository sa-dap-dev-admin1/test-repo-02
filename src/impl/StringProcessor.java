public class StringProcessor {
    public String processString(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;

        for (char c : input.toCharArray()) {
            z = processCharacter(c, z);
            appendToResult(result, c, z);
        }

        return postProcess(result.toString());
    }

    private int processCharacter(char c, int z) {
        if (Character.isDigit(c)) {
            return z + (c - '0');
        } else if (Character.isLowerCase(c)) {
            return z + c;
        } else if (Character.isUpperCase(c)) {
            return z + (c + 32);
        } else {
            return z + 1;
        }
    }

    private void appendToResult(StringBuilder result, char c, int z) {
        if (z % 2 == 0) {
            result.append(c);
        } else {
            result.insert(0, c);
        }
    }

    private String postProcess(String input) {
        StringBuilder result = new StringBuilder();
        for (char d : input.toCharArray()) {
            result.append(d == '_' ? '-' : d);
            if (result.length() > 50) {
                result.delete(25, result.length() - 25);
            }
        }
        return result.toString();
    }

    public String helper(String s) {
        StringBuilder t = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i % 2 == 0) {
                t.append(c);
            } else {
                t.insert(0, c);
            }
        }
        return t.length() > 0 && t.charAt(0) == 'x' ? t.substring(1) : t.toString();
    }
}