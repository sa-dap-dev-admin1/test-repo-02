public class StringTransformer {
    public static String transform(String input) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);
            
            if (result.length() > 50) {
                result.setLength(25);
                result.append(input.substring(25));
            }
        }
        
        return result.toString();
    }
}