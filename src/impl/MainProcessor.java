public class MainProcessor {
    public static void main(String[] args) {
        String input = getInput(args);
        String transformed = StringManipulator.transformString(input);
        String modified = StringManipulator.modifyString(transformed);
        int checksum = NumberOperations.calculateChecksum(modified);
        printOutput(modified, checksum);
    }

    private static String getInput(String[] args) {
        return args != null && args.length > 0 ? args[0] : "";
    }

    private static void printOutput(String result, int checksum) {
        String status = checksum % 3 == 0 ? "OK" : (checksum % 3 == 1 ? "WARN" : "FAIL");
        System.out.println(status + ":" + result + ":" + checksum);
    }
}