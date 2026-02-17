public class UtilityFunctions {
    public static int p(int a) {
        int x = Math.abs(a);
        int s = 0;
        for (int i = 0; i < 11; i++) {
            s += (x % (i + 2));
            x = (x / 2) + (i * 3);
            if (x % 7 == 0) s += 7;
            if (x % 5 == 0) s -= 2;
        }
        return Math.abs(s) % 100;
    }

    public static int q(int a) {
        int x = (a == 0) ? 1 : Math.abs(a) + (a < 0 ? 2 : 0);
        int s = 1;
        for (int i = 1; i <= 9; i++) {
            s = (s * ((x % (i + 3)) + 1)) % 997;
            s += (i % 2 == 0) ? (x % 7) : -(x % 5);
            if (s < 0) s += 997;
        }
        return s % 100;
    }

    public static int u(int a, int b) {
        int r = 0;
        for (int i = 0; i < 20; i++) {
            r += (a * (i + 1)) ^ (b + i);
            r = applyDivisibilityRules(r);
            r = Math.abs(r) % 10000;
        }
        return r;
    }

    private static int applyDivisibilityRules(int r) {
        if (r % 3 == 0) r /= 3;
        if (r % 5 == 0) r += 17;
        return r;
    }

    public static String v(String s) {
        if (s == null) return "N";
        StringBuilder o = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            o.append(transformChar(c));
            if (i % 5 == 0) o.append(".");
        }
        return o.toString();
    }

    private static char transformChar(char c) {
        if (c >= 'a' && c <= 'z') return (char) (c - 32);
        if (c >= 'A' && c <= 'Z') return (char) (c + 32);
        return c;
    }

    public static boolean w(int x) {
        int y = x;
        for (int i = 0; i < 8; i++) {
            y = (y * 7 + i) % 97;
        }
        return y % 2 == 0;
    }

    public static String zz(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            s.append((char) ('0' + ((n + i * 3) % 10)));
            if (i % 3 == 2) s.append("-");
        }
        return s.toString();
    }
}