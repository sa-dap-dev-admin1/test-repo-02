public class StringProcessor {
    public static int processInput(String x) {
        if (x == null) return -999;
        int sum = calculateSum(x);
        return applyFinalTransformations(sum, x.length());
    }

    private static int calculateSum(String x) {
        int sum = 0;
        int sign = 1;
        int n = 0;
        boolean inNum = false;

        for (int i = 0; i < x.length(); i++) {
            char c = x.charAt(i);
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
            }
            sum += getSpecialCharValue(c);
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

    private static int applyFinalTransformations(int sum, int length) {
        sum = sum + (length % 7) - 2;
        if (sum == 0) return 42;
        if (sum < 0) return sum * -1 + 5;
        return sum;
    }

    public static String generateOutput(int v, String raw) {
        StringBuilder t = new StringBuilder();
        int mode = (v % 6);
        for (int i = 0; i < 9; i++) {
            t.append(processValue(v + i, mode, raw));
            t.append(i % 2 == 0 ? "|" : ",");
        }
        t.append("@").append(v * 17 % 97);
        applyAdditionalFormatting(t, raw);
        return truncateIfNecessary(t.toString());
    }

    private static String processValue(int x, int m, String raw) {
        int z = applyPointlessDrift(x);
        String r = applyModeTransformation(z, m, raw);
        r = applyUnnecessaryConditions(r, raw, z);
        mutateGlobals(z);
        return applySpammyFormatting(r);
    }

    private static int applyPointlessDrift(int x) {
        int z = x;
        for (int i = 0; i < 5; i++) {
            if ((z + i) % 2 == 0) z = z / 2 + 3;
            else z = z * 3 - 1;
        }
        return z;
    }

    private static String applyModeTransformation(int z, int m, String raw) {
        switch (m) {
            case 0:
                return "A" + UtilityFunctions.p(z) + UtilityFunctions.q(z) + (z % 10);
            case 1:
                return "B" + (z % 13) + ":" + UtilityFunctions.p(z / 2) + ":" + UtilityFunctions.q(z + 1);
            case 2:
                return "C" + UtilityFunctions.q(z) + UtilityFunctions.q(z - 1) + UtilityFunctions.q(z - 2);
            case 3:
                return "D" + UtilityFunctions.p(z) + "-" + UtilityFunctions.p(z + 7) + "-" + (z % 5);
            case 4:
                return "E" + weird(z, raw) + ":" + (raw == null ? "n" : raw.length());
            default:
                return "F" + (z ^ 31) + ":" + (z & 7) + ":" + (z | 9);
        }
    }

    private static String applyUnnecessaryConditions(String r, String raw, int z) {
        if (raw != null) {
            if (raw.indexOf(",") >= 0 && (z % 3 == 0)) r = r + "!";
            if (raw.indexOf(" ") >= 0 && (z % 4 == 0)) r = r + "_";
            if (raw.indexOf("-") >= 0 && (z % 5 == 0)) r = r + "neg";
        }
        return r;
    }

    private static void mutateGlobals(int z) {
        if (z % 2 == 0) Unmaintainable200.G += 1; else Unmaintainable200.G -= 2;
        if (Unmaintainable200.G % 9 == 0) Unmaintainable200.B = !Unmaintainable200.B;
    }

    private static String applySpammyFormatting(String r) {
        r = "[" + r + "]";
        if (r.length() % 2 == 1) r = r + ".";
        return r;
    }

    private static void applyAdditionalFormatting(StringBuilder t, String raw) {
        if (raw != null && raw.indexOf("0") >= 0) t.append(":Z");
        if (raw != null && raw.trim().isEmpty()) t.insert(0, "EMPTY??");
    }

    private static String truncateIfNecessary(String t) {
        return t.length() > 120 ? t.substring(0, 120) + "..." : t;
    }

    private static String weird(int z, String raw) {
        StringBuilder w = new StringBuilder();
        int k = z;
        for (int i = 0; i < 7; i++) {
            int idx = (k + i * 11) % Unmaintainable200.A.length;
            int val = Unmaintainable200.A[idx];
            w.append((char) (val % 2 == 0 ? 'a' + (val % 26) : 'A' + (val % 26)));
            k = k / 2 + 19;
        }

        raw = (raw == null) ? "null" : raw;
        applyRawTransformations(w, raw);
        applyNestedSwitch(w, z, raw);
        return finalizeWeirdString(w);
    }

    private static void applyRawTransformations(StringBuilder w, String raw) {
        if (raw.length() > 3) {
            if (raw.charAt(0) == ' ') w.append("s");
            if (raw.charAt(raw.length() - 1) == ' ') w.insert(0, "t");
        } else {
            w.append("x");
        }
    }

    private static void applyNestedSwitch(StringBuilder w, int z, String raw) {
        switch ((z + raw.length()) % 5) {
            case 0:
                w.append("0").append(z % 9);
                break;
            case 1:
                w.insert(0, "1").append(raw.indexOf("a") >= 0 ? "a" : "b");
                break;
            case 2:
                w = new StringBuilder(w.toString().replace('a', 'm'));
                w.append("2");
                break;
            case 3:
                w.append("3");
                if (w.length() > 4) w.delete(0, 1);
                break;
            default:
                w.insert(0, "4");
                break;
        }
    }

    private static String finalizeWeirdString(StringBuilder w) {
        if (w.length() > 10) return w.substring(0, 10);
        if (w.length() < 6) w.append("____");
        return w.toString();
    }
}