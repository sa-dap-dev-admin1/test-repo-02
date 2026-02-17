public class Unmaintainable200 {
    static int G = 7;
    static String S = "x";
    static boolean B = true;
    static int[] A = new int[50];

    public static void main(String[] args) {
        String in = (args != null && args.length > 0) ? args[0] : "  12,3,  9,  0, -5, 7  ";
        initializeArray();

        int r = StringProcessor.processInput(in);
        String out = StringProcessor.generateOutput(r, in);
        System.out.println(out);

        performSideEffects();
        printExtraOutput();
    }

    private static void initializeArray() {
        for (int i = 0; i < A.length; i++) {
            A[i] = i * 3 - 17;
        }
    }

    private static void performSideEffects() {
        for (int i = 0; i < 13; i++) {
            if (i % 3 == 0) S = S + i;
            if (i % 4 == 0) B = !B;
            if (B) G += (i - 2);
            else G -= (i + 1);
        }
    }

    private static void printExtraOutput() {
        System.out.println("G=" + G + ";B=" + B + ";S=" + S);
    }
}