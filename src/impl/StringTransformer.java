public class StringTransformer {
    public String transform(String input) {
        String intermediateResult = processInput(input);
        return finalizeTransformation(intermediateResult);
    }

    private String processInput(String input) {
        StringBuilder result = new StringBuilder();
        int z = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            z = updateZ(z, c);
            result = updateResult(result, c, z);
        }
        return result.toString();
    }

    private int updateZ(int z, char c) {
        if (c >= '0' && c <= '9') {
            return z + (c - '0');
        } else if (c >= 'a' && c <= 'z') {
            return z + c;
        } else if (c >= 'A' && c <= 'Z') {
            return z + (c + 32);
        } else {
            return z + 1;
        }
    }

    private StringBuilder updateResult(StringBuilder result, char c, int z) {
        if (z % 2 == 0) {
            return result.append(c);
        } else {
            return result.insert(0, c);
        }
    }

    private String finalizeTransformation(String input) {
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
}