public class StringTransformer {
    public static String transform(String input) {
        StringBuilder result = new StringBuilder();
        int accumulator = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            accumulator += getCharValue(c);

            if (accumulator % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }

        return result.toString();
    }

    private static int getCharValue(char c) {
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
}