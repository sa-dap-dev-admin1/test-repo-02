public class CharacterProcessor {
    public static String processString(String input) {
        StringBuilder processedString = new StringBuilder();
        int accumulator = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            accumulator += processCharacter(c);

            if (accumulator % 2 == 0) {
                processedString.append(c);
            } else {
                processedString.insert(0, c);
            }
        }

        return processedString.toString();
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
}