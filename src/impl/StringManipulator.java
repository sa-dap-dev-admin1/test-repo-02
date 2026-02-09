public class StringManipulator {
    private static final int MAX_STRING_LENGTH = 50;

    public static String manipulateString(String input) {
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            result.append(c == '_' ? '-' : c);
            if (result.length() > MAX_STRING_LENGTH) {
                result.delete(25, result.length());
            }
        }

        return result.toString();
    }
}