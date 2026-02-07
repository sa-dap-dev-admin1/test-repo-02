public class OutputGenerator {
    public static void generateOutput(String transformedString, int checksum) {
        String status = getStatus(checksum);
        System.out.println(status + ":" + transformedString + ":" + checksum);
    }

    private static String getStatus(int checksum) {
        switch (checksum % 3) {
            case 0:
                return "OK";
            case 1:
                return "WARN";
            default:
                return "FAIL";
        }
    }
}