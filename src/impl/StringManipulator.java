public class StringManipulator {
    public static String alternateReverseString(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (i % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }
}