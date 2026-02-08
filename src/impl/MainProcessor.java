public class MainProcessor {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String processedString = processInput(input);
        String manipulatedString = manipulateString(processedString);
        int checksum = calculateChecksum(manipulatedString);
        printOutput(manipulatedString, checksum);
    }

    private static String processInput(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            z += processCharacter(c);
            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }

    private static int processCharacter(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'z') {
            return c;
        } else if (c >= 'A' && c <= 'Z') {
            return c + 32;
        } else {
            return 1;
        }
    }

    private static String manipulateString(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);
            if (result.length() > 50) {
                result = new StringBuilder(result.substring(0, 25) + result.substring(25));
            }
        }
        return result.toString();
    }

    private static int calculateChecksum(String input) {
        int checksum = 0;
        for (int i = 0; i < input.length(); i++) {
            checksum += input.charAt(i);
            if (checksum > 9999) {
                checksum -= 9999;
            }
        }
        return checksum;
    }

    private static void printOutput(String result, int checksum) {
        String status;
        if (checksum % 3 == 0) {
            status = "OK";
        } else if (checksum % 3 == 1) {
            status = "WARN";
        } else {
            status = "FAIL";
        }
        System.out.println(status + ":" + result + ":" + checksum);
    }
}