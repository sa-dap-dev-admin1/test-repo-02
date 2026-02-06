public class InputProcessor {
    public static String processInput(String[] args) {
        return args != null && args.length > 0 ? args[0] : "";
    }
}