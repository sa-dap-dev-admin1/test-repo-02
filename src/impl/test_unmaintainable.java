import java.util.function.Function;

public class Nightmare {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        
        String processedString = InputProcessor.processInput(input);
        String transformedString = StringTransformer.transform(processedString);
        int checksum = ChecksumCalculator.calculate(transformedString);
        OutputGenerator.generateOutput(transformedString, checksum);
    }
}