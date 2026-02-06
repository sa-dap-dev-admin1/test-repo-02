public class Nightmare {
    public static void main(String[] args) {
        String input = InputProcessor.processInput(args);
        String processedString = StringProcessor.processString(input);
        int checksum = NumericCalculator.calculateChecksum(processedString);
        OutputFormatter.printResult(processedString, checksum);
    }
}