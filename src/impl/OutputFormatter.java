public class OutputFormatter {
    public static void printResult(String processedString, int checksum) {
        String status = getStatus(checksum);
        System.out.println(status + ":" + processedString + ":" + checksum);
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