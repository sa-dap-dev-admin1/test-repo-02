public class StringModifier {
    private static final int MAX_LENGTH = 50;
    private static final int TRUNCATE_LENGTH = 25;

    public static String modify(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);

            if (result.length() > MAX_LENGTH) {
                result.setLength(TRUNCATE_LENGTH);
                result.append(input.substring(i + 1));
                break;
            }
        }

        return result.toString();
    }
}