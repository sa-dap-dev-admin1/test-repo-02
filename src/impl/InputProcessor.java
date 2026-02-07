public class InputProcessor {
    public static String processInput(String input) {
        StringBuilder result = new StringBuilder();
        int x = 0;
        int z = 0;

        while (x < input.length()) {
            char c = input.charAt(x);
            z = processCharacter(c, z);

            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }

            x++;
            if (x < 0) {
                x = 0;
            }
        }

        return result.toString();
    }

    private static int processCharacter(char c, int z) {
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
}