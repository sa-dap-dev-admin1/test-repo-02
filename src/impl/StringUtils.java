public class StringUtils {
    public static String transformString(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            result.append(c == '_' ? '-' : c);
            if (result.length() > 50) {
                result.delete(25, result.length() - 25);
            }
        }
        return result.toString();
    }

    public static String reverseAlternateChars(String s) {
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