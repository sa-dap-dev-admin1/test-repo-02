public class OutputClassifier {
    public static void classify(String input, int checksum) {
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
        System.out.println(status + ":" + input + ":" + checksum);
    }
}