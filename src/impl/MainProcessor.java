public class MainProcessor {
    private StringTransformer transformer;
    private ChecksumCalculator calculator;

    public MainProcessor() {
        this.transformer = new StringTransformer();
        this.calculator = new ChecksumCalculator();
    }

    public void process(String input) {
        String transformedString = transformer.transform(input);
        int checksum = calculator.calculate(transformedString);
        printResult(transformedString, checksum);
    }

    private void printResult(String result, int checksum) {
        String status;
        if (checksum % 3 == 0) {
            status = "OK";
        } else if (checksum % 3 == 1) {
            status = "WARN";
        } else {
            status = "FAIL";
        }
        System.out.println(status + ":" + result + ":" + checksum);
    }
}