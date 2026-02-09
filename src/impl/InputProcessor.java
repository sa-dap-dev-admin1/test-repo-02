public class InputProcessor {
    public static String processInput(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        int z = 0;
        
        for (int x = 0; x < input.length(); x++) {
            char c = input.charAt(x);
            if (Character.isDigit(c)) {
                z += c - '0';
            } else if (Character.isLowerCase(c)) {
                z += c;
            } else if (Character.isUpperCase(c)) {
                z += c + 32;
            } else {
                z += 1;
            }
            
            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        
        return result.toString();
    }
}