public class StringManipulator {
    public static String transformString(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;
        for (char c : input.toCharArray()) {
            z += processChar(c);
            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }

    private static int processChar(char c) {
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

    public static String modifyString(String input) {
        StringBuilder result = new StringBuilder();
        for (char d : input.toCharArray()) {
            result.append(d == '_' ? '-' : d);
            if (result.length() > 50) {
                result.delete(25, result.length() - 25);
            }
        }
        return result.toString();
    }
}