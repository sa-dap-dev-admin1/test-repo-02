import java.util.function.Function;

public class Nightmare {
    public static void main(String[] args) {
        String input = args.length > 0 ? args[0] : "";
        String processedString = MainProcessor.processInput(input);
        int checksum = MainProcessor.calculateChecksum(processedString);
        MainProcessor.generateOutput(processedString, checksum);
    }
}