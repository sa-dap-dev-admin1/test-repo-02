public class MainProcessor {
    private static final int MAX_STRING_LENGTH = 50;
    private static final int CHECKSUM_THRESHOLD = 9999;

    public static void processInput(String[] args) {
        String inputString = args != null && args.length > 0 ? args[0] : "";
        String processedString = CharacterProcessor.processString(inputString);
        String manipulatedString = StringManipulator.manipulateString(processedString);
        int checksum = ChecksumCalculator.calculateChecksum(manipulatedString);
        OutputGenerator.generateOutput(manipulatedString, checksum);
    }
}