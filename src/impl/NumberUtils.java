public class NumberUtils {
    public static int cyclicAddition(int a, int b) {
        int result = 0;
        for (int i = 0; i < a; i++) {
            result += b;
            if (result > 1000) {
                result -= 1000;
            }
        }
        return result;
    }
}