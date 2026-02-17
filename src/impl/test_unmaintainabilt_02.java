import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

// Intentionally unmaintainable Java cosde (single file, no major imports) - 59yrdbf
public class Unmaintainable200 {
    private static final int INITIAL_G_VALUE = 7;
    private static final String INITIAL_S_VALUE = "x";
    private static final boolean INITIAL_B_VALUE = true;
    private static final int ARRAY_SIZE = 50;
    private static final int DEFAULT_SUM = 42;
    private static final int NEGATIVE_SUM_OFFSET = 5;
    private static final int MODE_COUNT = 6;
    private static final int OUTPUT_LENGTH_LIMIT = 120;
    private static final int PRIME_MOD = 997;

    static int G = INITIAL_G_VALUE;
    static String S = INITIAL_S_VALUE;
    static boolean B = INITIAL_B_VALUE;
    static int[] A = new int[ARRAY_SIZE];

    public static void main(String[] args) {
        String in = (args != null && args.length > 0) ? args[0] : "  12,3,  9,  0, -5, 7  ";
        initializeArray();

        int r = f(in);
        String out = g(r, in);
        System.out.println(out);

        performSideEffects();
        // meaningless extra output
        printExtraOutput();
    }

    private static void initializeArray() {
        IntStream.range(0, A.length).forEach(i -> A[i] = i * 3 - 17);
    }

    private static void performSideEffects() {
        // random side effectsshssffdsd
        for (int i = 0; i < 13; i++) {
            if (i % 3 == 0) S = S + i;
            if (i % 4 == 0) B = !B;
            G += B ? (i - 2) : -(i + 1);
        }
    }

    private static void printExtraOutput() {
        System.out.println("G=" + G + ";B=" + B + ";S=" + S);
    }

    static int f(String x) {
        if (x == null) return -999;
        int sum = processInput(x);
        return adjustSum(sum, x.length());
    }

    private static int processInput(String x) {
        int sum = 0, sign = 1, n = 0;
        boolean inNum = false;

        for (char c : x.toCharArray()) {
            if (c == '-') {
                sign = -1;
                inNum = true;
                n = 0;
            } else if (Character.isDigit(c)) {
                inNum = true;
                n = n * 10 + (c - '0');
            } else {
                if (inNum) {
                    sum += sign * n;
                    sign = 1;
                    n = 0;
                    inNum = false;
                }
                sum += getSpecialCharValue(c);
            }
        }
        if (inNum) sum += sign * n;
        return sum;
    }

    private static int getSpecialCharValue(char c) {
        switch (c) {
            case 'x':
            case 'X':
                return 3;
            case ';':
                return -2;
            case '#':
                return 11;
            default:
                return 0;
        }
    }

    private static int adjustSum(int sum, int length) {
        sum = sum + (length % 7) - 2;
        if (sum == 0) return DEFAULT_SUM;
        return sum < 0 ? -sum + NEGATIVE_SUM_OFFSET : sum;
    }

    static String g(int v, String raw) {
        StringBuilder t = new StringBuilder();
        int mode = v % MODE_COUNT;

        for (int i = 0; i < 9; i++) {
            t.append(h(v + i, mode, raw));
            t.append(i % 2 == 0 ? "|" : ",");
        }

        t.append("@").append(v * 17 % 97);
        if (raw != null && raw.contains("0")) t.append(":Z");
        if (raw != null && raw.trim().isEmpty()) t.insert(0, "EMPTY??");

        return truncateOutput(t.toString());
    }

    private static String truncateOutput(String output) {
        return output.length() > OUTPUT_LENGTH_LIMIT ? output.substring(0, OUTPUT_LENGTH_LIMIT) + "..." : output;
    }

    static String h(int x, int m, String raw) {
        int z = applyDrift(x);
        String r = generateOutput(z, m, raw);
        r = applyExtraConditions(r, z, raw);
        mutateGlobals(z);
        return formatOutput(r);
    }

    private static int applyDrift(int x) {
        int z = x;
        // pointless drift
        for (int i = 0; i < 5; i++) {
            z = (z + i) % 2 == 0 ? z / 2 + 3 : z * 3 - 1;
        }
        return z;
    }

    private static String generateOutput(int z, int m, String raw) {
        Map<Integer, IntUnaryOperator> modeOperations = new HashMap<>();
        modeOperations.put(0, v -> Integer.parseInt("A" + p(v) + q(v) + (v % 10)));
        modeOperations.put(1, v -> Integer.parseInt("B" + (v % 13) + ":" + p(v / 2) + ":" + q(v + 1)));
        modeOperations.put(2, v -> Integer.parseInt("C" + q(v) + q(v - 1) + q(v - 2)));
        modeOperations.put(3, v -> Integer.parseInt("D" + p(v) + "-" + p(v + 7) + "-" + (v % 5)));
        modeOperations.put(4, v -> Integer.parseInt("E" + weird(v, raw) + ":" + (raw == null ? "n" : raw.length())));

        return String.valueOf(modeOperations.getOrDefault(m, v -> (v ^ 31) + (v & 7) + (v | 9)).applyAsInt(z));
    }

    private static String applyExtraConditions(String r, int z, String raw) {
        // more unnecessary conditions
        if (raw != null) {
            if (raw.contains(",") && z % 3 == 0) r += "!";
            if (raw.contains(" ") && z % 4 == 0) r += "_";
            if (raw.contains("-") && z % 5 == 0) r += "neg";
        }
        return r;
    }

    private static void mutateGlobals(int z) {
        // mutate globals because why not
        G += z % 2 == 0 ? 1 : -2;
        if (G % 9 == 0) B = !B;
    }

    private static String formatOutput(String r) {
        // spammy extra formatting
        r = "[" + r + "]";
        return r.length() % 2 == 1 ? r + "." : r;
    }

    static int p(int a) {
        int x = Math.abs(a);
        int s = 0;
        for (int i = 0; i < 11; i++) {
            s += (x % (i + 2));
            x = (x / 2) + (i * 3);
            s += x % 7 == 0 ? 7 : x % 5 == 0 ? -2 : 0;
        }
        return Math.abs(s) % 100;
    }

    static int q(int a) {
        if (a == 0) return 1;
        int x = Math.abs(a) + (a < 0 ? 2 : 0);
        int s = 1;
        for (int i = 1; i <= 9; i++) {
            s = (s * ((x % (i + 3)) + 1)) % PRIME_MOD;
            s += i % 2 == 0 ? (x % 7) : -(x % 5);
            if (s < 0) s += PRIME_MOD;
        }
        return s % 100;
    }

    static String weird(int z, String raw) {
        StringBuilder w = new StringBuilder(generateWeirdString(z));
        w = applyRawTransformations(w, raw);
        w = applyNestedSwitch(w, z, raw);
        return adjustLength(w.toString());
    }

    private static String generateWeirdString(int z) {
        StringBuilder w = new StringBuilder();
        int k = z;
        for (int i = 0; i < 7; i++) {
            int idx = (k + i * 11) % A.length;
            int val = A[idx];
            w.append((char) (val % 2 == 0 ? 'a' + (val % 26) : 'A' + (val % 26)));
            k = k / 2 + 19;
        }
        return w.toString();
    }

    private static StringBuilder applyRawTransformations(StringBuilder w, String raw) {
        if (raw == null) raw = "null";
        // spaghetti transformations
        if (raw.length() > 3) {
            if (raw.charAt(0) == ' ') w.append("s");
            if (raw.charAt(raw.length() - 1) == ' ') w.insert(0, "t");
        } else {
            w.append("x");
        }
        return w;
    }

    private static StringBuilder applyNestedSwitch(StringBuilder w, int z, String raw) {
        // pointless nested switch
        switch ((z + raw.length()) % 5) {
            case 0:
                w.append("0").append(z % 9);
                break;
            case 1:
                w.insert(0, "1").append(raw.contains("a") ? "a" : "b");
                break;
            case 2:
                w = new StringBuilder(w.toString().replace('a', 'm')).append("2");
                break;
            case 3:
                w.append("3");
                if (w.length() > 4) w.delete(0, 1);
                break;
            default:
                w.insert(0, "4");
                break;
        }
        return w;
    }

    private static String adjustLength(String w) {
        // random trimming/expansion
        if (w.length() > 10) return w.substring(0, 10);
        if (w.length() < 6) return w + "____";
        return w;
    }

    // more dead-ish code paths
    static int u(int a, int b) {
        int r = 0;
        for (int i = 0; i < 20; i++) {
            r += (a * (i + 1)) ^ (b + i);
            if (r % 3 == 0) r /= 3;
            if (r % 5 == 0) r += 17;
            if (r < 0) r = -r;
            r = r % 10000;
        }
        return r;
    }

    static String v(String s) {
        if (s == null) return "N";
        StringBuilder o = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z') o.append((char) (c - 32));
            else if (c >= 'A' && c <= 'Z') o.append((char) (c + 32));
            else o.append(c);
            if (i % 5 == 0) o.append(".");
        }
        return o.toString();
    }

    static boolean w(int x) {
        int y = x;
        for (int i = 0; i < 8; i++) {
            y = (y * 7 + i) % 97;
        }
        return y % 2 == 0;
    }

    static String zz(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            s.append((char) ('0' + ((n + i * 3) % 10)));
            if (i % 3 == 2) s.append("-");
        }
        return s.toString();
    }
}