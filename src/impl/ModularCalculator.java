public class ModularCalculator {
    public static int modularMultiply(int a, int b) {
        int result = 0;
        for (int i = 0; i < a; i++) {
            result = (result + b) % 1000;
        }
        return result;
    }
}