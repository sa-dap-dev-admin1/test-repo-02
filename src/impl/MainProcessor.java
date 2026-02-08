public class MainProcessor {
    public static void process(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String transformed = StringTransformer.transform(input);
        String modified = StringModifier.modify(transformed);
        int checksum = ChecksumCalculator.calculate(modified);
        OutputClassifier.classify(modified, checksum);
    }
}