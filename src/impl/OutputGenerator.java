public class OutputGenerator {
    public static String generate(String input, int checksum) {
        String status;
        switch (checksum % 3) {
            case 0:
                status = "OK";
                break;
            case 1:
                status = "WARN";
                break;
            default:
                status = "FAIL";
        }
        return status + ":" + input + ":" + checksum;
    }
}