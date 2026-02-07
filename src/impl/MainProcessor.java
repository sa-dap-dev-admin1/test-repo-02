import java.util.function.Function;

public class MainProcessor {
    public static String processInput(String input) {
        StringBuilder result = new StringBuilder();
        int sum = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sum += processCharacter(c);
            
            if (sum % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        
        return StringUtils.transformString(result.toString());
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
    
    public static int calculateChecksum(String input) {
        int checksum = 0;
        for (char c : input.toCharArray()) {
            checksum += c;
            if (checksum > 9999) {
                checksum -= 9999;
            }
        }
        return checksum;
    }
    
    public static void generateOutput(String processedString, int checksum) {
        String status;
        if (checksum % 3 == 0) {
            status = "OK";
        } else if (checksum % 3 == 1) {
            status = "WARN";
        } else {
            status = "FAIL";
        }
        System.out.println(status + ":" + processedString + ":" + checksum);
    }
}