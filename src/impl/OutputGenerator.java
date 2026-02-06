public class OutputGenerator {
    public void generateOutput(String finalString, int checksum) {
        String status = determineStatus(checksum);
        System.out.println(status + ":" + finalString + ":" + checksum);
    }

    private String determineStatus(int checksum) {
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