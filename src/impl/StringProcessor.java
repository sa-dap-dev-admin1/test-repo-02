public class StringProcessor {
    public static String processInput(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            z += processCharacter(c);

            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }

        return postProcess(result.toString());
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

    private static String postProcess(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);
            if (result.length() > 50) {
                result.delete(25, result.length() - 25);
            }
        }
        return result.toString();
    }
}