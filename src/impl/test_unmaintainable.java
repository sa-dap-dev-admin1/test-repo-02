public class Nightmare {
    public static void main(String[] args) {
        InputProcessor inputProcessor = new InputProcessor();
        StringTransformer stringTransformer = new StringTransformer();
        OutputGenerator outputGenerator = new OutputGenerator();

        String input = inputProcessor.validateInput(args);
        String transformedString = inputProcessor.processInput(input);
        String finalString = stringTransformer.transformString(transformedString);
        int checksum = stringTransformer.calculateChecksum(finalString);
        outputGenerator.generateOutput(finalString, checksum);
    }
}