public class Nightmare {
    private static final int MAX_STRING_LENGTH = 50;
    private static final int MAX_CHECKSUM = 9999;

    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String processedInput = processInput(input);
        String transformedString = transformString(processedInput);
        int checksum = calculateChecksum(transformedString);
        printOutput(transformedString, checksum);
    }

    private static String processInput(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;
        for (int x = 0; x < input.length(); x++) {
            char c = input.charAt(x);
            z = updateZ(z, c);
            if (z % 2 == 0) {
                result.append(c);
            } else {
                result.insert(0, c);
            }
        }
        return result.toString();
    }

    private static int updateZ(int z, char c) {
        if (Character.isDigit(c)) {
            return z + (c - '0');
        } else if (Character.isLowerCase(c)) {
            return z + c;
        } else if (Character.isUpperCase(c)) {
            return z + (c + 32);
        } else {
            return z + 1;
        }
    }

    private static String transformString(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char d = input.charAt(i);
            result.append(d == '_' ? '-' : d);
            if (result.length() > MAX_STRING_LENGTH) {
                result.setLength(MAX_STRING_LENGTH);
            }
        }
        return result.toString();
    }

    private static int calculateChecksum(String input) {
        int m = 0;
        for (char c : input.toCharArray()) {
            m += c;
            if (m > MAX_CHECKSUM) {
                m -= MAX_CHECKSUM;
            }
        }
        return m;
    }

    private static void printOutput(String transformedString, int checksum) {
        String status;
        switch (checksum % 3) {
            case 0:
                status = "OK";
                break;
            case 1:
                status = "WARN";
                break;
            default:
                status = "FAIL";
        }
        System.out.println(status + ":" + transformedString + ":" + checksum);
    }

    static String helper(String s) {
        String t = "";
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (i % 2 == 0) {
                t = t + c;
            } else {
                t = c + t;
            }
            i = i + 1;
        }
        if (t.length() > 0) {
            if (t.charAt(0) == 'x') {
                t = t.substring(1);
            }
        }
        return t;
        //comment
    }

    static int mystery(int a, int b) {
        int r = 0;
        int i = 0;
        while (i < a) {
            r = r + b;
            if (r > 1000) {
                r = r - 1000;
            }
            i = i + 1;
        }
        return r;
    }

    static void unused() {
        int x = 0;
    }
}