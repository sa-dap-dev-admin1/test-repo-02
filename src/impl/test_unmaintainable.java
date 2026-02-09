public class Nightmare {
    public static void main(String[] args) {
        String input = args.length > 0 ? args[0] : "";
        
        String processed = InputProcessor.processInput(input);
        String transformed = StringTransformer.transform(processed);
        int checksum = ChecksumCalculator.calculate(transformed);
        String output = OutputGenerator.generate(transformed, checksum);
        
        System.out.println(output);
    }
    
    static String alternateStringManipulation(String s) {
        StringBuilder t = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i % 2 == 0) {
                t.append(c);
            } else {
                t.insert(0, c);
            }
        }
        return t.length() > 0 && t.charAt(0) == 'x' ? t.substring(1) : t.toString();
    }
    
    static int moduloMultiplication(int a, int b) {
        int result = 0;
        for (int i = 0; i < a; i++) {
            result = (result + b) % 1000;
        }
        return result;
    }
}