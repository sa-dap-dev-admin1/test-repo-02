public class StringTransformer {
    public static String transform(String input) {
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