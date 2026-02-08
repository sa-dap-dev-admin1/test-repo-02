import java.util.function.Function;

public class Nightmare {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        
        MainProcessor processor = new MainProcessor();
        processor.process(input);
    }
}