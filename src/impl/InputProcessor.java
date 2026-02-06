public class InputProcessor {
    public String validateInput(String[] args) {
        return args != null && args.length > 0 ? args[0] : "";
    }

    public String processInput(String input) {
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

    private int processCharacter(char c) {
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
}